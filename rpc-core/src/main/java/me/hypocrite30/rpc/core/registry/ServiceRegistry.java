package me.hypocrite30.rpc.core.registry;

import me.hypocrite30.rpc.common.extension.SPI;

import java.net.InetSocketAddress;

/**
 * service registration
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/7 22:45
 */
@SPI
public interface ServiceRegistry {
    /**
     * register service
     *
     * @param rpcServiceName    rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
