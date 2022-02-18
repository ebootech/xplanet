package tech.eboot.xplanet.remoting.protocol;

/**
 * @author TangThree
 * Created on 2022/1/31 1:55 AM
 **/
public class DecodeException extends RuntimeException{
    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }

    public DecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
