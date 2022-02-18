package tech.eboot.xplanet.remoting.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import tech.eboot.xplanet.remoting.Disposable;
import tech.eboot.xplanet.remoting.protocol.MessageDecoder;
import tech.eboot.xplanet.remoting.protocol.MessageEncoder;
import tech.eboot.xplanet.remoting.util.RemotingUtil;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangThree
 * Created on 2022/1/29 8:54 PM
 **/

@Slf4j
public class NettyServer implements Disposable
{
    private final NettyServerConfig nettyServerConfig;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossLoopGroup;
    private EventLoopGroup selectorLoopGroup;
    private EventLoopGroup workerLoopGroup;
    private EventLoopGroup serviceLoopGroup;
    private ChannelHandler[] serviceChannelHandlers;

    public NettyServer(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    private void init()
    {
        bootstrap = new ServerBootstrap();
        if (useEpoll()) {
            bossLoopGroup = new EpollEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            selectorLoopGroup = new EpollEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerLoopGroup = new EpollEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            serviceLoopGroup = new EpollEventLoopGroup(nettyServerConfig.getServiceThreads(), new ThreadFactory() {
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
            selectorLoopGroup = new NioEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerSelectorThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            workerLoopGroup = new NioEventLoopGroup(nettyServerConfig.getSelectorThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
            serviceLoopGroup = new NioEventLoopGroup(nettyServerConfig.getServiceThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerServiceThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
    }

    public void registerServiceChannelHandler(ChannelHandler...channelHandlers) {
        this.serviceChannelHandlers = channelHandlers;
    }

    public void start(){
        init();
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
                        channel.pipeline()
                                .addLast(workerLoopGroup,
                                        new MessageEncoder(),
                                        new MessageDecoder(),
                                        new HeartbeatHandler(nettyServerConfig.getReaderIdleTimeSeconds(), 0, 0),
                                        new NettyServerConnectManagerHandler())
                                .addLast(serviceLoopGroup, serviceChannelHandlers);
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(nettyServerConfig.getPort()).sync();
            if (future.isSuccess()) {
                log.info("NettyServer is running on port: {}", nettyServerConfig.getPort());
            } else {
                throw new NettyServerStartException("failed to start NettyServer", future.cause());
            }

        } catch (InterruptedException e) {
            throw new NettyServerStartException(e);
        }
    }

    @Override
    public void destroy() {
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
        log.info("NettyServer has been destroyed");
    }

    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform()
                && nettyServerConfig.isUseEpoll()
                && Epoll.isAvailable();
    }
}
