package tech.eboot.xplanet.remoting.service;

import java.lang.annotation.*;

/**
 * @author TangThree
 * Created on 2022/1/31 11:26 PM
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NettyService {

    /**
     * service name
     * @return
     */
    String value();
}
