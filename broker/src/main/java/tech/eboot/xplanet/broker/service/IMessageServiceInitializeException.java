package tech.eboot.xplanet.broker.service;

/**
 * @author TangThree
 * Created on 2022/2/1 4:21 PM
 **/
public class IMessageServiceInitializeException extends RuntimeException{
    public IMessageServiceInitializeException() {
    }

    public IMessageServiceInitializeException(String message) {
        super(message);
    }

    public IMessageServiceInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IMessageServiceInitializeException(Throwable cause) {
        super(cause);
    }

    public IMessageServiceInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
