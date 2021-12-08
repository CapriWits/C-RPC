package me.hypocrite30.rpc.core.provider;

import me.hypocrite30.rpc.core.config.RpcServiceConfig;

/**
 * @Description: store and provide service object
 * @Author: Hypocrite30
 * @Date: 2021/11/20 22:12
 */
public interface ServiceProvider {

    /**
     * @param rpcServiceConfig rpc service config
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service config
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
