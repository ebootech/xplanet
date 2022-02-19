package tech.eboot.xplanet.broker;

import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;

/**
 * @author TangThree
 * Created on 2022/2/18 12:22
 **/
public class BrokerApplication
{
    public static void main(String[] args)
    {

    }

    public static void start(String[] args)
    {
        NettyServerConfig config = new NettyServerConfig();
        NettyServer server = new NettyServer(config);
        server.start();
    }
}
