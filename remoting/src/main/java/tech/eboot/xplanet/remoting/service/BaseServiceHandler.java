package tech.eboot.xplanet.remoting.service;

import tech.eboot.xplanet.common.util.JsonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author TangThree
 * Created on 2022/2/1 4:01 PM
 **/
public abstract class BaseServiceHandler<T> implements NettyServiceHandler {

    private final Type messageType;

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
    public Object handleMessage(MessageContext context, long messageId, String body) {
        Object message = (messageType == CharSequence.class || messageType == String.class) ? body : JsonUtils.readObject(body, messageType);
        return processMessage(context, messageId, (T)message);
    }

    protected abstract Object processMessage(MessageContext context, long messageId, T message);
}
