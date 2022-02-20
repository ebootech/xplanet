package tech.eboot.xplanet.broker.dispatch;

import org.springframework.stereotype.Service;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.service.MessageContext;
import tech.eboot.xplanet.remoting.service.NettyServiceHandler;

/**
 * @author TangThree
 * Created on 2022/2/20 4:49 PM
 **/

@Service
public class DispatchService implements NettyServiceHandler {
    @Override
    public Object handleMessage(MessageContext context, Message message) {
        return null;
    }
}
