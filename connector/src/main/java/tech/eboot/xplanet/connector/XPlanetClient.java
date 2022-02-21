package tech.eboot.xplanet.connector;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import tech.eboot.xplanet.remoting.client.NettyClient;
import tech.eboot.xplanet.remoting.client.NettyClientConfig;
import tech.eboot.xplanet.remoting.protocol.Message;

/**
 * @author TangThree
 * Created on 2022/2/19 12:52 PM
 **/

@Slf4j
public class XPlanetClient
{
    private NettyClientConfig clientConfig;
    private NettyClient client;
    private MessageListener messageListener;

    public XPlanetClient(NettyClientConfig clientConfig, MessageListener messageListener)
    {
        this.clientConfig = clientConfig;
        this.messageListener = messageListener;
        init();
    }

    private void init()
    {
        client = new NettyClient(clientConfig);
        client.registerChannelHandler(new SimpleChannelInboundHandler<Message>(){
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
            {
                messageListener.onMessage(ctx.channel(), msg);
            }
        });
    }

    public ChannelFuture connect(int port)
    {
        return client.connect(port);
    }

    public ChannelFuture connect(String host, int port)
    {
        return client.connect(host, port);
    }
}
