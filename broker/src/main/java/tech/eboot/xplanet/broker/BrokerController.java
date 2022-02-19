package tech.eboot.xplanet.broker;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import tech.eboot.xplanet.remoting.server.NettyServer;
import tech.eboot.xplanet.remoting.server.NettyServerConfig;

/**
 * @author TangThree
 * Created on 2022/2/20 3:18 AM
 **/

@Component
public class BrokerController implements InitializingBean, DisposableBean
{
    private NettyServer nettyServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        nettyServer = new NettyServer(new NettyServerConfig());
        nettyServer.registerChannelHandler(new ChannelDuplexHandler(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                super.channelRead(ctx, msg);
            }
        });
        nettyServer.start().sync();
    }

    @Override
    public void destroy() throws Exception {
        nettyServer.shutdown();
    }
}
