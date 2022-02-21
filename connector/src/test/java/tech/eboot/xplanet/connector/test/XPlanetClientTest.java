package tech.eboot.xplanet.connector.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.eboot.xplanet.connector.MessageListener;
import tech.eboot.xplanet.connector.XPlanetClient;
import tech.eboot.xplanet.remoting.client.NettyClientConfig;
import tech.eboot.xplanet.remoting.protocol.Message;
import tech.eboot.xplanet.remoting.protocol.MessageType;
import tech.eboot.xplanet.remoting.service.ServiceMessageBody;
import tech.eboot.xplanet.remoting.util.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TangThree
 * Created on 2022/2/19 12:53 PM
 **/
@Slf4j
public class XPlanetClientTest
{

    public static void main(String[] args)
    {
        int a = 10;
        byte b = 10;
        System.out.println(a==b);
    }

    @Test
    public void connectTest() throws InterruptedException
    {

        //新建一个消息监听器. 用于接收消息
        MessageListener messageListener = new MessageListener()
        {
            @Override
            public void onMessage(Channel channel, Message message)
            {
                log.info("收到消息: {}", message);
            }
        };

        //创建一个客户端
        XPlanetClient client = new XPlanetClient(new NettyClientConfig(), messageListener);

        //连接服务器
        Channel channel;
        client.connect(10937).addListener(new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if (future.isSuccess()) {
                    //连接成功后的回调

                    ServiceMessageBody body = new ServiceMessageBody();
                    body.setService("login");
                    Map<String,Object> map = new HashMap<>();
                    map.put("token", "123");
                    body.setBody(JsonUtils.toJson(map));
                    Message message = Message.fromPayload(MessageType.SERVICE.getValue(), JsonUtils.toJson(body));
                    for (int i = 0; i < 1; i++) {
                        //登录服务器
                        future.channel().writeAndFlush(message);
                    }

                } else {
                    //连接失败...
                }
            }
        });

        Thread.sleep(100000000);
    }
}
