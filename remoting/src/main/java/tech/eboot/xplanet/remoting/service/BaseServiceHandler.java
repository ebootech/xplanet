package tech.eboot.xplanet.remoting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.util.StringConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author TangThree
 * Created on 2022/2/1 4:01 PM
 **/
public abstract class BaseServiceHandler<T> implements NettyServiceHandler {

    private final Type messageType;
    protected final Logger log= LoggerFactory.getLogger(this.getClass());

    public BaseServiceHandler() {
        Class thisClass = this.getClass();
        Class superClass = null;
        Type superType = null;
        while (true) {
            superClass = thisClass.getSuperclass();
            if (superClass == BaseServiceHandler.class) {
                superType = thisClass.getGenericSuperclass();
                if (!(superType instanceof ParameterizedType)) {
                    throw new ServiceInitializeException(String.format("[%s] must be ParameterizedType"));
                }
                this.messageType = ((ParameterizedType)superType).getActualTypeArguments()[0];
                break;
            }
            thisClass = thisClass.getSuperclass();
        }
    }

    @Override
    public Object handleMessage(MessageContext context, Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        Object messageBody = StringConverter.stringToObj(body, messageType);
        return handleMessage(context, message.getId(), (T)messageBody);
    }

    protected abstract Object handleMessage(MessageContext context, long messageId, T message);
}
