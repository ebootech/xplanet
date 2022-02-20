package tech.eboot.xplanet.broker.dispatch;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tech.eboot.xplanet.broker.properties.BrokerProperties;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;
import tech.eboot.xplanet.remoting.server.ServerStartupException;
import tech.eboot.xplanet.remoting.service.ServiceDispatcherChannelHandler;

/**
 * @author TangThree
 * Created on 2022/2/20 8:21 PM
 **/

@Slf4j
@Configuration
public class DispatcherServerConfiguration implements InitializingBean, DisposableBean
{
    @Autowired
    BrokerProperties brokerProperties;
    @Autowired
    DispatchService dispatchService;

    private NettyServer nettyServer;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {
        NettyServerConfig config = brokerProperties.getDispatcher().getServer();
        ServiceDispatcherChannelHandler dispatcherChannelHandler = new ServiceDispatcherChannelHandler();
        dispatcherChannelHandler.registerService("dispatch", dispatchService);
        nettyServer = new NettyServer(config);
        nettyServer.registerChannelHandler(dispatcherChannelHandler);
        try {
            nettyServer.start().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("MessageServer is running on port: {}", config.getPort());
                    } else {
                        log.error("Fail to start MessageServer", future.cause());
                        throw new ServerStartupException(future.cause());
                    }
                }
            }).sync();
        } catch (InterruptedException e) {
            log.error("MessageServer is interrupted!", e);
        }
    }


}
