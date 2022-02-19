package tech.eboot.xplanet.broker.service;

/**
 * @author TangThree
 * Created on 2022/1/31 11:11 PM
 **/
public interface IMessageServiceHandler {

    Object handleMessage(MessageContext context, long messageId, String body);

}
