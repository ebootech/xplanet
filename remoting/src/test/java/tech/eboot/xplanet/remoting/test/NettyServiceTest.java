package tech.eboot.xplanet.remoting.test;

import org.junit.jupiter.api.Test;
import tech.eboot.xplanet.remoting.client.NettyClient;
import tech.eboot.xplanet.remoting.client.NettyClientConfig;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;

/**
 * @author TangThree
 * Created on 2022/2/18 13:13
 **/
public class NettyServiceTest
{
    @Test
    public void nettyServer()
    {
        NettyServer server = new NettyServer(new NettyServerConfig());
        try {
            server.start().sync();
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nettyClient()
    {
        NettyClient client = new NettyClient(new NettyClientConfig());
        try {
            client.connect(10937).sync();
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
