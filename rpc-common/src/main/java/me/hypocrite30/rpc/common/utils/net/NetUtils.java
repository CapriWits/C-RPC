package me.hypocrite30.rpc.common.utils.net;

import me.hypocrite30.rpc.common.exception.RpcException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
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
                        // site-local address is needed, excluding host like /10.*
                        if (inetAddress.isSiteLocalAddress() && !inetAddress.toString().matches("\\/10.*")) {
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

    /**
     * split the socket address from '/' & ':' to get InetSocketAddress
     *
     * @param inetSocketAddressStr e.g. /192.168.224.1:9998
     * @return InetSocketAddress with host + port, e.g. 192.168.224.1:9998
     */
    public static InetSocketAddress newInetSocketAddress(String inetSocketAddressStr) {
        String[] split = inetSocketAddressStr.split(":");
        Integer port = Integer.valueOf(split[1]);
        String hostName = split[0].split("/")[1];
        return new InetSocketAddress(hostName, port);
    }
}
