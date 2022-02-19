package tech.eboot.xplanet.remoting.client;

import lombok.Getter;
import lombok.Setter;
import tech.eboot.xplanet.remoting.util.NettyConfigUtil;

/**
 * @author TangThree
 * Created on 2022/2/19 2:27 PM
 **/

@Setter
@Getter
public class NettyClientConfig
{
    private String host                 = "127.0.0.1";
    private int port                    = 10937;
    private int workerThreads           = NettyConfigUtil.availableProcessors;
    private int userThreads             = NettyConfigUtil.availableProcessors * 2;
    private int connectTimeoutSeconds   = 30;
    private int reconnectSeconds        = 3;
    private int readerIdleTimeSeconds   = 60;
    private int writerIdleTimeSeconds   = 30;

}
