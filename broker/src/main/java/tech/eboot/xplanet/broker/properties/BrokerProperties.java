package tech.eboot.xplanet.broker.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;

/**
 * @author TangThree
 * Created on 2022/2/20 4:23 PM
 **/


@ConfigurationProperties("broker")
@Configuration
@Setter
@Getter
public class BrokerProperties
{
    private String name;
    private MessageServerConfig message;
    private DispatcherServerConfig dispatcher;


    @Setter
    @Getter
    public class MessageServerConfig
    {
        private NettyServerConfig server;
    }

    @Setter
    @Getter
    public class DispatcherServerConfig
    {
        private NettyServerConfig server;
    }
}
