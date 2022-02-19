package tech.eboot.xplanet.remoting.client;

import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;
import tech.eboot.xplanet.remoting.NettyAbstract;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.protocol.MessageDecoder;
import tech.eboot.xplanet.remoting.protocol.MessageEncoder;
import tech.eboot.xplanet.remoting.util.NettyConfigUtil;
import tech.eboot.xplanet.remoting.util.RemotingHelper;
import tech.eboot.xplanet.remoting.util.RemotingUtil;
import tech.eboot.xplanet.remoting.util.ServerAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/2/19 6:58 PM
 **/

@Slf4j
public class NettyPoolClient extends NettyAbstract{
    private final NettyPoolClientConfig clientConfig;
    private Bootstrap bootstrap;
    private EventLoopGroup selectorEventGroup;
    private EventLoopGroup workerEventGroup;
    private EventLoopGroup userEventGroup;
    private ChannelPoolMap<String, FixedChannelPool> channelPoolMap;
    private final Map<String, InetSocketAddress> inetSocketAddressMap = new HashMap<>();

    public NettyPoolClient(NettyPoolClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    private void init()
    {
        this.bootstrap = new Bootstrap();
        this.selectorEventGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelectorThread_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.workerEventGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientWorkerThread_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.userEventGroup = new NioEventLoopGroup(clientConfig.getUserThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientWorkerThread_%d", this.threadIndex.incrementAndGet()));
            }
        });

        bootstrap.group(selectorEventGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutSeconds() * 1000);

        String serverAddress = clientConfig.getServerAddress();
        ServerAddress[] serverAddresses = NettyConfigUtil.resolveServerAddress(serverAddress);
        for (ServerAddress address : serverAddresses) {
            String serverName = address.getName();
            if (StrUtil.isBlank(serverName)) {
                serverName = address.getHost() + ":" + address.getPort();
            }
            inetSocketAddressMap.put(serverName, new InetSocketAddress(address.getHost(), address.getPort()));
        }

        channelPoolMap = new AbstractChannelPoolMap<String, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(String key) {
                InetSocketAddress inetSocketAddress = null;
                if (StrUtil.isBlank(key)) {
                    throw new IllegalArgumentException("The ServerName must not be null");
                }
                inetSocketAddress= inetSocketAddressMap.get(key);
                return new FixedChannelPool(
                        bootstrap.remoteAddress(inetSocketAddress),
                        new ChannelPoolHandler() {
                            @Override
                            public void channelReleased(Channel channel) throws Exception {
                                log.debug("NETTY CLIENT ChannelReleased: {}", RemotingHelper.parseChannelRemoteAddr(channel));
                            }

                            @Override
                            public void channelAcquired(Channel channel) throws Exception {
                                log.debug("NETTY CLIENT ChannelAcquired: {}", RemotingHelper.parseChannelRemoteAddr(channel));
                            }

                            @Override
                            public void channelCreated(Channel channel) throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(workerEventGroup,
                                        new MessageDecoder(),
                                        new MessageEncoder(),
                                        new IdleStateHandler(clientConfig.getReaderIdleTimeSeconds(), clientConfig.getWriterIdleTimeSeconds(), 0),
                                        new ConnectManagerHandler());
                                if (channelHandlers.size() > 0) {
                                    pipeline.addLast(userEventGroup, channelHandlers.toArray(new ChannelHandler[]{}));
                                }
                            }
                        },
                        clientConfig.getMaxConnectionsEveryServer());
            }
        };
    }

    public List<Future<Channel>> start() {
        List<Future<Channel>> futures = new ArrayList<>();
        for (String serverName : inetSocketAddressMap.keySet()) {
            FixedChannelPool pool = channelPoolMap.get(serverName);
            Future<Channel> future = pool.acquire();
            future.addListener(new FutureListener<Channel>(){
                @Override
                public void operationComplete(Future<Channel> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("NettyClient is connected to RemotingServer: {}", future.getNow().remoteAddress());
                    } else {
                        log.error("Connected to RemotingServer:{} failure!", future.getNow().remoteAddress(), future.cause());
                    }
                }
            });
            futures.add(future);
        }
        return futures;
    }

    public void shutdown() {
        if (selectorEventGroup != null) {
            selectorEventGroup.shutdownGracefully();
        }
        if (workerEventGroup != null) {
            workerEventGroup.shutdownGracefully();
        }
        if (userEventGroup != null) {
            userEventGroup.shutdownGracefully();
        }
        inetSocketAddressMap.clear();
    }

    class ConnectManagerHandler extends ChannelDuplexHandler {
        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                            ChannelPromise promise) throws Exception {
            final String local = localAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(localAddress);
            final String remote = remoteAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(remoteAddress);
            log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
            super.close(ctx, promise);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.WRITER_IDLE)) {
                    log.debug("NETTY CLIENT PIPELINE: WRITER_IDLE");
                    ctx.channel().writeAndFlush(Message.newHeartbeatMessage());
                } else if (event.state().equals(IdleState.READER_IDLE)) {
                    log.info("NETTY CLIENT PIPELINE: READER_IDLE");
                    RemotingUtil.closeChannel(ctx.channel());
                }
            }

            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
            log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
            RemotingUtil.closeChannel(ctx.channel());
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("NETTY CLIENT PIPELINE: WRITE {}", msg);
            super.write(ctx, msg, promise);
        }
    }
}
