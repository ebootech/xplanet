package tech.eboot.xplanet.broker;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.eboot.xplanet.broker.message.LoginService;
import tech.eboot.xplanet.broker.properties.BrokerProperties;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;
import tech.eboot.xplanet.remoting.server.ServerStartupException;
import tech.eboot.xplanet.remoting.service.ServiceDispatcherChannelHandler;

/**
 * @author TangThree
 * Created on 2022/2/20 3:18 AM
 **/

@Slf4j
@Component
public class BrokerBootstrap implements InitializingBean, DisposableBean
{
    @Autowired
    BrokerProperties brokerProperties;

    @Autowired
    LoginService loginService;

    private NettyServer messageServer;
    private NettyServer dispatchServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        startMessageServer();
        startDispatchServer();
    }

    @Override
    public void destroy() throws Exception {
        messageServer.shutdown();
    }

    private void startMessageServer()
    {
        NettyServerConfig config = brokerProperties.getMessage().getServer();
        ServiceDispatcherChannelHandler dispatcherChannelHandler = new ServiceDispatcherChannelHandler();
        dispatcherChannelHandler.registerService("login", loginService);
        messageServer = new NettyServer(config);
        messageServer.registerChannelHandler(dispatcherChannelHandler);
        try {
            messageServer.start().addListener(new ChannelFutureListener() {
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

    private void startDispatchServer()
    {

    }
}
