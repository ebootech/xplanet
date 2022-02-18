package tech.eboot.xplanet.remoting.server;

/**
 * @author TangThree
 * Created on 2022/1/31 2:38 AM
 **/
public class NettyServerStartException extends RuntimeException{
    public NettyServerStartException() {
    }

    public NettyServerStartException(String message) {
        super(message);
    }

    public NettyServerStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public NettyServerStartException(Throwable cause) {
        super(cause);
    }

    public NettyServerStartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
