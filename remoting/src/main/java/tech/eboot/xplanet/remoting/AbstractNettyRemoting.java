package tech.eboot.xplanet.remoting;

import io.netty.bootstrap.Bootstrap;

/**
 * @author TangThree
 * Created on 2022/2/18 16:26
 **/
public abstract class AbstractNettyRemoting implements NettyRemoting
{
    protected Bootstrap bootstrap = new Bootstrap();

}
