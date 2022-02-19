package tech.eboot.xplanet.remoting;

import io.netty.channel.ChannelHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author TangThree
 * Created on 2022/2/18 16:26
 **/
public abstract class NettyAbstract
{
    protected List<ChannelHandler> channelHandlers = new ArrayList<>();

    public void registerChannelHandler(ChannelHandler...channelHandlers)
    {
        this.channelHandlers.addAll(Arrays.asList(channelHandlers));
    }
}
