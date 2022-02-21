package tech.eboot.xplanet.broker.message;

import org.springframework.stereotype.Service;
import tech.eboot.xplanet.remoting.service.BaseServiceHandler;
import tech.eboot.xplanet.remoting.service.MessageContext;

/**
 * @author TangThree
 * Created on 2022/2/20 4:41 PM
 **/

@Service
public class LoginService extends BaseServiceHandler<LoginRequest> {
    @Override
    protected Object handleMessage(MessageContext context, long messageId, LoginRequest message) {
        log.info("login successfully, accessToken:{}", message.getToken());
        return null;
    }
}
