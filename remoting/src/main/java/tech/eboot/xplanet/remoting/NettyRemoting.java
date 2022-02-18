package tech.eboot.xplanet.remoting;

import io.netty.channel.ChannelFuture;

/**
 * @author TangThree
 * Created on 2022/2/18 16:27
 **/
public interface NettyRemoting
{
    ChannelFuture start();

    void shutdown();
}
