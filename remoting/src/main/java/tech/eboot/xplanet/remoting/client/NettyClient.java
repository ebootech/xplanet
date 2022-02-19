package tech.eboot.xplanet.remoting.client;

import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import tech.eboot.xplanet.remoting.NettyAbstract;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.protocol.MessageDecoder;
import tech.eboot.xplanet.remoting.protocol.MessageEncoder;
import tech.eboot.xplanet.remoting.util.RemotingHelper;
import tech.eboot.xplanet.remoting.util.RemotingUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton Connection, Only use for client test.
 * @author TangThree
 * Created on 2022/2/18 16:26
 **/

@Slf4j
public class NettyClient extends NettyAbstract
{
    private final NettyClientConfig clientConfig;
    private boolean isInitialized = false;
    private Bootstrap bootstrap;
    private EventLoopGroup selectorEventGroup;
    private EventLoopGroup workerEventGroup;
    private EventLoopGroup userEventGroup;
    private final ScheduledExecutorService reconnectExecutorService = Executors.newScheduledThreadPool(1);

    public NettyClient(NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    private void init()
    {
        if (isInitialized) {
            return;
        }
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutSeconds() * 1000)
                .handler(new ChannelInitializer<NioSocketChannel>(){
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
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
                });
        isInitialized = true;
    }

    public ChannelFuture connect() {
        init();
        InetSocketAddress address = StrUtil.isBlank(clientConfig.getHost())
                ? new InetSocketAddress(clientConfig.getPort())
                : new InetSocketAddress(clientConfig.getHost(), clientConfig.getPort());
        log.info("Try Connect to RemotingServer:{}...", clientConfig.getHost() + ":" + clientConfig.getPort());
        ChannelFuture future = bootstrap.connect(address);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("NettyClient is connected to RemotingServer: {}", future.channel().remoteAddress());
                } else {
                    log.error(String.format("Fail to connected to RemotingServer:%s", future.channel().remoteAddress()), future.cause());
                    if (!reconnectExecutorService.isShutdown()) {
                        int reconnectSeconds = clientConfig.getReconnectSeconds();
                        log.info("Try reconnect after {} Seconds", reconnectSeconds);
                        reconnectExecutorService.schedule(new Runnable() {
                            @Override
                            public void run() {
                                connect();
                            }
                        }, reconnectSeconds, TimeUnit.SECONDS);
                    }
                }
            }
        });
        return future;
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
        reconnectExecutorService.shutdownNow();
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
