package tech.eboot.xplanet.remoting.server;

import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/2/18 16:28
 **/
@Slf4j
public class NettyServer extends NettyAbstract
{
    private final NettyServerConfig serverConfig;
    private boolean isInitialized = false;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossLoopGroup;
    private EventLoopGroup selectorLoopGroup;
    private EventLoopGroup workerLoopGroup;
    private EventLoopGroup serviceLoopGroup;

    public NettyServer(NettyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    private void init()
    {
        if (isInitialized) {
            return;
        }
        bootstrap = new ServerBootstrap();
        if (useEpoll()) {
            bossLoopGroup = new EpollEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            selectorLoopGroup = new EpollEventLoopGroup(serverConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerLoopGroup = new EpollEventLoopGroup(serverConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            serviceLoopGroup = new EpollEventLoopGroup(serverConfig.getServiceThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerServiceThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            bossLoopGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            selectorLoopGroup = new NioEventLoopGroup(serverConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerLoopGroup = new NioEventLoopGroup(serverConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            serviceLoopGroup = new NioEventLoopGroup(serverConfig.getServiceThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerServiceThread_%d", this.threadIndex.incrementAndGet()));
                }
            });

            isInitialized = true;
        }

        bootstrap.group(bossLoopGroup, selectorLoopGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024*1024, 4*1024*1024))
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(workerLoopGroup,
                                new MessageEncoder(),
                                new MessageDecoder(),
                                new HeartbeatHandler(serverConfig.getReaderIdleTimeSeconds(), 0, 0),
                                new ConnectManagerChannelHandler());
                        if (channelHandlers.size() > 0) {
                            pipeline.addLast(serviceLoopGroup, channelHandlers.toArray(new ChannelHandler[]{}));
                        }

                    }
                });
    }

    public ChannelFuture start(){
        init();
        InetSocketAddress address = StrUtil.isBlank(serverConfig.getHost())
                ? new InetSocketAddress(serverConfig.getPort())
                : new InetSocketAddress(serverConfig.getHost(), serverConfig.getPort());
        ChannelFuture future = bootstrap.bind(address);
        /*future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.debug("NettyServer is running on port: {}", serverConfig.getPort());
                } else {
                    log.debug("Fail to start NettyServer", future.cause());
                }
            }
        });*/
        return future;
    }

    public void shutdown() {
        if (bossLoopGroup != null) {
            bossLoopGroup.shutdownGracefully();
        }
        if (selectorLoopGroup != null) {
            selectorLoopGroup.shutdownGracefully();
        }
        if (workerLoopGroup != null) {
            workerLoopGroup.shutdownGracefully();
        }
        if (serviceLoopGroup != null) {
            serviceLoopGroup.shutdownGracefully();
        }
    }

    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform()
                && serverConfig.isUseEpoll()
                && Epoll.isAvailable();
    }


    class HeartbeatHandler extends IdleStateHandler {

        public HeartbeatHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
            super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Message message = (Message) msg;
            if (message.isHeartMessage()) {
                ctx.channel().writeAndFlush(message.newHeartbeatReplyMessage());
            }
            super.channelRead(ctx, msg);
        }
    }

    class ConnectManagerChannelHandler extends ChannelDuplexHandler {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.READER_IDLE)) {
                    final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("NETTY SERVER PIPELINE: READER_IDLE exception [{}]", remoteAddress);
                    RemotingUtil.closeChannel(ctx.channel());
                }
            }

            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.error(String.format("NETTY SERVER PIPELINE: exceptionCaught, remoteAddress:%s", remoteAddress), cause);
            RemotingUtil.closeChannel(ctx.channel());
        }
    }
}
