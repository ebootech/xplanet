package tech.eboot.xplanet.broker.router;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tech.eboot.xplanet.broker.BrokerProperties;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.ServerStartupException;
import tech.eboot.xplanet.remoting.service.ServiceDispatcherChannelHandler;

/**
 * @author TangThree
 * Created on 2022/2/20 8:21 PM
 **/

@Slf4j
@Configuration
public class RouterConfiguration implements InitializingBean, DisposableBean
{
    @Autowired
    BrokerProperties brokerProperties;
    @Autowired
    MessageRedirectService messageRedirectService;

    private NettyServer nettyServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        RouterProperties routerProperties = brokerProperties.getRouter();
        ServiceDispatcherChannelHandler dispatcherChannelHandler = new ServiceDispatcherChannelHandler();
        dispatcherChannelHandler.registerService("message-redirect", messageRedirectService);
        nettyServer = new NettyServer(routerProperties);
        nettyServer.registerChannelHandler(dispatcherChannelHandler);
        try {
            nettyServer.start().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("RouterServer is running on port: {}", routerProperties.getPort());
                    } else {
                        log.error("Fail to start RouterServer", future.cause());
                        throw new ServerStartupException(future.cause());
                    }
                }
            }).sync();
        } catch (InterruptedException e) {
            log.error("RouterServer is interrupted!", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        nettyServer.shutdown();
        log.warn("RouterServer is shutdown!");
    }


}
