package tech.eboot.xplanet.remoting.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TangThree
 * Created on 2022/1/30 3:12 PM
 **/

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageType {
    HEARTBEAT        ((byte) 0),
    REPLY            ((byte) 1),
    SERVICE          ((byte) 2),
    ;

    private byte value;
}
