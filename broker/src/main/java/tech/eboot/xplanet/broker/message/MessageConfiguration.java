package tech.eboot.xplanet.broker.message;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.eboot.xplanet.broker.BrokerProperties;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.ServerStartupException;
import tech.eboot.xplanet.remoting.service.ServiceDispatcherChannelHandler;

/**
 * @author TangThree
 * Created on 2022/2/20 8:21 PM
 **/

@Slf4j
@Component
public class MessageConfiguration implements InitializingBean, DisposableBean
{
    @Autowired
    BrokerProperties brokerProperties;
    @Autowired
    LoginService loginService;

    private NettyServer nettyServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageProperties messageProperties = brokerProperties.getMessage();
        ServiceDispatcherChannelHandler channelHandler = new ServiceDispatcherChannelHandler();
        channelHandler.registerService("login", loginService);
        nettyServer = new NettyServer(messageProperties);
        nettyServer.registerChannelHandler(channelHandler);
        try {
            nettyServer.start().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("MessageServer is running on port: {}", messageProperties.getPort());
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

    @Override
    public void destroy() throws Exception {
        log.warn("MessageServer is shutdown!");
        nettyServer.shutdown();
    }

}
