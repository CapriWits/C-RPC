package me.hypocrite30.rpc.core.registry;

import me.hypocrite30.rpc.common.extension.SPI;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * service discovery
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/8 20:11
 */
@SPI
public interface ServiceDiscovery {
    /**
     * find service by service name
     *
     * @param rpcRequest rpc service object
     * @return service address
     */
    InetSocketAddress findService(RpcRequest rpcRequest);
}
