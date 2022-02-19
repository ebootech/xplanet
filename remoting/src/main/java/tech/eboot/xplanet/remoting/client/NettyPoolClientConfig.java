package tech.eboot.xplanet.remoting.client;

import lombok.Getter;
import lombok.Setter;
import tech.eboot.xplanet.remoting.util.NettyConfigUtil;

/**
 * @author TangThree
 * Created on 2022/2/19 7:16 PM
 **/

@Setter
@Getter
public class NettyPoolClientConfig {
    private String serverAddress          = "server1-127.0.0.1:10937";
    private int workerThreads             = 1;
    private int userThreads               = NettyConfigUtil.availableProcessors;
    private int maxConnectionsEveryServer = NettyConfigUtil.availableProcessors;
    private int connectTimeoutSeconds     = 30;
    private int reconnectSeconds          = 3;
    private int readerIdleTimeSeconds     = 60;
    private int writerIdleTimeSeconds     = 30;

}
