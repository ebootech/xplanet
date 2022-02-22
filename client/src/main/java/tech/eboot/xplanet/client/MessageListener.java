package tech.eboot.xplanet.client;

import io.netty.channel.Channel;
import tech.eboot.xplanet.remoting.protocol.Message;

/**
 * @author TangThree
 * Created on 2022/2/21 15:16
 **/
public interface MessageListener
{
    void onMessage(Channel channel, Message message);
}
