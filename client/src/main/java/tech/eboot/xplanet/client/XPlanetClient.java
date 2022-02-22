package tech.eboot.xplanet.client;

import tech.eboot.xplanet.remoting.protocol.Message;

import java.util.concurrent.TimeUnit;

/**
 * @author TangThree
 * Created on 2022/2/21 9:58 PM
 **/
public interface XPlanetClient
{
    void start();

    Message syncSend(Message message);

    Message syncSend(Message message, int timeoutSeconds);

    Message syncSend(Message message, long timeout, TimeUnit timeUnit);

    void asyncSend(Message message, Callback callback);

    void shutdown();
}
