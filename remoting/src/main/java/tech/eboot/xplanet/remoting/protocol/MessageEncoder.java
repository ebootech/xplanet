package tech.eboot.xplanet.remoting.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * part 1. 1byte (magic code)
 * part 2. 8byte (message id)
 * part 3. 1byte (message type)
 * part 4. 4byte (body length)
 * part 5. body
 * @author TangThree
 * Created on 2022/1/29 8:42 PM
 **/

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeByte(Message.MAGIC_CODE);
        out.writeLong(msg.getId());
        out.writeByte(msg.getType());
        byte[] body = msg.getBody();
        if (body == null || body.length == 0) {
            out.writeInt(0);
        } else {
            out.writeInt(body.length);
            out.writeBytes(msg.getBody());
        }
        log.debug("WRITE MESSAGE:{}", msg);
    }
}
