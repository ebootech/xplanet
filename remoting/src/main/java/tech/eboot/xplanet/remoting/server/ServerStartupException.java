package tech.eboot.xplanet.remoting.server;

/**
 * @author TangThree
 * Created on 2022/2/20 4:39 PM
 **/
public class ServerStartupException extends Exception{
    public ServerStartupException() {
    }

    public ServerStartupException(String message) {
        super(message);
    }

    public ServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerStartupException(Throwable cause) {
        super(cause);
    }

    public ServerStartupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
