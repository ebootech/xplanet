package tech.eboot.xplanet.remoting.util;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.StringUtil;

/**
 * @author TangThree
 * Created on 2022/2/19 2:29 PM
 **/
public class NettyConfigUtil {
    public static final int availableProcessors = NettyRuntime.availableProcessors();

    public static ServerAddress[] resolveServerAddress(String serverAddress)
    {
        if (StringUtil.isNullOrEmpty(serverAddress) || !serverAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid serverAddress: " + serverAddress);
        }
        String[] addrs = serverAddress.split(";");
        ServerAddress[] addresses = new ServerAddress[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            String addr = addrs[i];
            if (!addr.contains(":")) {
                throw new IllegalArgumentException("Invalid serverAddress: " + serverAddress);
            }
            ServerAddress address = new ServerAddress();
            if (addr.contains("-")) {
                String[] nameInetAddrParts = addr.split("-");
                if (nameInetAddrParts.length != 2) {
                    throw new IllegalArgumentException("Invalid serverAddress: " + serverAddress);
                }
                address.setName(nameInetAddrParts[0]);
                String[] inetAddrParts = nameInetAddrParts[1].split(":");
                if (inetAddrParts.length != 2) {
                    throw new IllegalArgumentException("Invalid serverAddress: " + serverAddress);
                }
                address.setHost(inetAddrParts[0]);
                address.setPort(Integer.valueOf(inetAddrParts[1]));
            } else {
                String[] inetAddrParts = addr.split(":");
                if (inetAddrParts.length != 2) {
                    throw new IllegalArgumentException("Invalid serverAddress: " + serverAddress);
                }
                address.setHost(inetAddrParts[0]);
                address.setPort(Integer.valueOf(inetAddrParts[1]));
            }

            addresses[i] = address;
        }
        return addresses;
    }
}
