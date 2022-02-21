package tech.eboot.xplanet.broker;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import tech.eboot.xplanet.broker.message.MessageProperties;
import tech.eboot.xplanet.broker.router.RouterProperties;

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
    private MessageProperties message;
    private RouterProperties router;
}
