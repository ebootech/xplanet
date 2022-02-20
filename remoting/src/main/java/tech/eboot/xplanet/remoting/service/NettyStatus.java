package tech.eboot.xplanet.remoting.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TangThree
 * Created on 2022/1/31 11:41 PM
 **/

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NettyStatus {

    SUCCESS           (0),
    BAD_REQUEST       (-1),
    SERVICE_NOT_FOUND (-2),
    INTERNAL_ERROR    (-3),
    ;
    @Getter
    private int val;
}
