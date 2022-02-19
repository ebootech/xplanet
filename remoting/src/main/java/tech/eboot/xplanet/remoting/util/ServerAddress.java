package tech.eboot.xplanet.remoting.util;

import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/2/20 1:14 AM
 **/

@Setter
@Getter
public class ServerAddress {
    private String name;
    private String host;
    private Integer port;
}
