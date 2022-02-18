package tech.eboot.xplanet.remoting.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;
import tech.eboot.xplanet.remoting.protocol.Message;

/**
 * @author TangThree
 * Created on 2022/1/31 9:21 PM
 **/
public class HeartbeatHandler extends IdleStateHandler {

    public HeartbeatHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (message.isHeartMessage()) {
            ctx.channel().writeAndFlush(message.newHeartbeatReplyMessage());
        }
        super.channelRead(ctx, msg);
    }
}
