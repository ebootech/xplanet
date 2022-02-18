package tech.eboot.xplanet.remoting;

import io.netty.util.NettyRuntime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/1/30 8:15 PM
 **/

@Setter
@Getter
public class NettyServerConfig {

    private static final int availableProcessors = NettyRuntime.availableProcessors();

    private boolean useEpoll             = true;

    private int selectorThreads          = availableProcessors * 2;

    private int workerThreads            = availableProcessors * 4;

    private int serviceThreads           = availableProcessors * 8;

    private int port                     = 10937;

    private int readerIdleTimeSeconds    = 60;

    private int maxAuthTimeout           = 30;

    public static void main(String[] args) {
        System.out.println(availableProcessors);
    }

}
