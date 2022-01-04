package me.hypocrite30.rpc.common.utils.net;

import me.hypocrite30.rpc.common.exception.RpcException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @Author: Hypocrite30
 * @Date: 2022/1/4 12:23
 */
public class NetUtils {

    private NetUtils() {
    }

    /**
     * Get site local address firstly or get local host
     *
     * @return exact local host
     */
    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> inetAddrs = networkInterface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddrs.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        // site-local address is needed
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress;
                        }
                        // As candidate if not site-local address
                        if (candidateAddress == null) {
                            candidateAddress = inetAddress;
                        }
                    }
                }
            }
            // get local host if candidate address does not exist
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            throw new RpcException("Getting network interface error", e);
        }
    }
}
