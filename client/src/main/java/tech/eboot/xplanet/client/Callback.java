package tech.eboot.xplanet.client;

import tech.eboot.xplanet.remoting.protocol.Message;

/**
 * @author TangThree
 * Created on 2022/2/22 15:39
 **/
public interface Callback
{
    void onSuccess(Message message);

    void onException(Exception cause);
}
