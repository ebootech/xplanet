package tech.eboot.xplanet.broker.service;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import tech.eboot.xplanet.common.util.JsonUtils;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.protocol.MessageType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangThree
 * Created on 2022/1/31 11:09 PM
 **/

@Slf4j
@ChannelHandler.Sharable
public class ServiceDispatcherChannelHandler extends SimpleChannelInboundHandler<Message> {

    private Map<String, IMessageServiceHandler> nettyServices = new ConcurrentHashMap<>();

    public void registerService(String serviceName, IMessageServiceHandler IMessageServiceHandler) {
        if (nettyServices.containsKey(serviceName)) {
            throw new RuntimeException(String.format("The service [%s] has been registered", serviceName));
        }
        nettyServices.put(serviceName, IMessageServiceHandler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message.getType() != MessageType.SERVICE.getValue()) {
            return;
        }
        ReplyMessage replyMessage = new ReplyMessage();
        ServiceMessageBody messageBody;
        try {
            messageBody = JsonUtils.readObject(message.getBody(), ServiceMessageBody.class);
        } catch (Exception e) {
            log.error("parse message body fail", e);
            replyMessage.setStatus(NettyStatus.BAD_REQUEST, "Cannot parse message body");
            writeResponse(ctx, message, replyMessage);
            return;
        }
        String serviceName = messageBody.getService();
        if (StringUtils.isEmpty(serviceName)) {
            replyMessage.setStatus(NettyStatus.BAD_REQUEST, "The Service must not be null");
            writeResponse(ctx, message, replyMessage);
            return;
        }
        IMessageServiceHandler serviceHandler = nettyServices.get(serviceName);
        if (serviceHandler == null) {
            replyMessage.setStatus(NettyStatus.SERVICE_NOT_FOUND, String.format("The Service [%s] is not exist", serviceName));
            writeResponse(ctx, message, replyMessage);
            return;
        }

        try {
            MessageContext messageContext = new MessageContext();
            messageContext.setCtx(ctx);
            Object responseBody = serviceHandler.handleMessage(messageContext, message.getId(),
                    new String(message.getBody(), StandardCharsets.UTF_8));
            replyMessage.setStatus(NettyStatus.SUCCESS.getVal());
            replyMessage.setData(responseBody);
            writeResponse(ctx, message, replyMessage);
        } catch (Exception e) {
            log.error(String.format("Execute %s causeException", serviceHandler), e);
            replyMessage.setStatus(NettyStatus.INTERNAL_ERROR, "internal error");
            writeResponse(ctx, message, replyMessage);
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, Message source, ReplyMessage response) {
        log.debug("netty response:{}", response);
        ctx.writeAndFlush(source.newReplyMessage(JsonUtils.toJson(response)));
    }
}
