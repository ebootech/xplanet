package tech.eboot.xplanet.remoting.service;

import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/1/31 11:36 PM
 **/

@Setter
@Getter
public class ServiceMessageBody {
    private String service;
    private String body;
}
