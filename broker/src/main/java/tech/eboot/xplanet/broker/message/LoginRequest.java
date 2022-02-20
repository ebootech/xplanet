package tech.eboot.xplanet.broker.message;

import lombok.Getter;
import lombok.Setter;

/**
 * @author TangThree
 * Created on 2022/2/20 4:45 PM
 **/

@Setter
@Getter
public class LoginRequest {
    private String token;
}
