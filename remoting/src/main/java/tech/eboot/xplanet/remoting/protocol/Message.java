package tech.eboot.xplanet.remoting.protocol;

import cn.hutool.core.lang.Snowflake;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author TangThree
 * Created on 2022/1/29 8:40 PM
 **/

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message
{
    private static final Snowflake snowflake = new Snowflake(
            new Random().nextInt(31),
            new Random().nextInt(31),
            true);

    public static final byte MAGIC_CODE = 88;

    private long id;
    private byte type;
    private byte[] body;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", type=" + type +
                ", body=" + (body == null ? null : new String(body, StandardCharsets.UTF_8)) +
                '}';
    }

    public Message newHeartbeatReplyMessage() {
        return newReplyMessage(null);
    }

    public Message newReplyMessage(String payload) {
        return Message.builder()
                .id(id)
                .type(MessageType.REPLY.getValue())
                .body(payload == null ? null : payload.getBytes(StandardCharsets.UTF_8))
                .build();
    }

    public boolean isHeartMessage() {
        return this.type == MessageType.HEARTBEAT.getValue();
    }

    public static Message newHeartbeatMessage() {
        return Message.builder()
                .id(snowflake.nextId())
                .type(MessageType.HEARTBEAT.getValue())
                .build();
    }

    public static Message fromPayload(byte type, String payload) {
        return fromPayload(type, payload == null ? null : payload.getBytes(StandardCharsets.UTF_8));
    }

    public static Message fromPayload(byte type, byte[] payload) {
        return Message.builder()
                .id(snowflake.nextId())
                .type(type)
                .body(payload)
                .build();
    }


}
