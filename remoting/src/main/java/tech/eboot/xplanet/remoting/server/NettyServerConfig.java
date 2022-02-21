package tech.eboot.xplanet.remoting.server;

import io.netty.util.NettyRuntime;
import lombok.Getter;
import lombok.Setter;
import tech.eboot.xplanet.remoting.util.NettyConfigUtil;

/**
 * @author TangThree
 * Created on 2022/1/30 8:15 PM
 **/

@Setter
@Getter
public class NettyServerConfig {
    private String host;
    private int port                     = 10937;
    private int selectorThreads          = NettyConfigUtil.availableProcessors * 2;
    private int workerThreads            = NettyConfigUtil.availableProcessors * 4;
    private int serviceThreads           = NettyConfigUtil.availableProcessors * 8;
    private boolean useEpoll             = true;
    private int readerIdleTimeSeconds    = 60;
    private int maxAuthTimeout           = 30;

    @Override
    public String toString()
    {
        return "NettyServerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", selectorThreads=" + selectorThreads +
                ", workerThreads=" + workerThreads +
                ", serviceThreads=" + serviceThreads +
                ", useEpoll=" + useEpoll +
                ", readerIdleTimeSeconds=" + readerIdleTimeSeconds +
                ", maxAuthTimeout=" + maxAuthTimeout +
                '}';
    }
}
