package tech.eboot.xplanet.broker.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/1/31 11:12 PM
 **/

@Setter
@Getter
public class MessageContext {
    private ChannelHandlerContext ctx;
}
