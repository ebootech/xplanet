package tech.eboot.xplanet.remoting.service;

import tech.eboot.xplanet.remoting.protocol.Message;

/**
 * @author TangThree
 * Created on 2022/1/31 11:11 PM
 **/
public interface NettyServiceHandler {

    Object handleMessage(MessageContext context, Message message);

}
