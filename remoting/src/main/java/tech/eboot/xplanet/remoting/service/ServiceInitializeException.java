package tech.eboot.xplanet.remoting.service;

/**
 * @author TangThree
 * Created on 2022/2/1 4:21 PM
 **/
public class ServiceInitializeException extends RuntimeException{
    public ServiceInitializeException() {
    }

    public ServiceInitializeException(String message) {
        super(message);
    }

    public ServiceInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceInitializeException(Throwable cause) {
        super(cause);
    }

    public ServiceInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
