package me.hypocrite30.rpc.core.provider.impl;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.provider.ServiceProvider;
import me.hypocrite30.rpc.core.registry.ServiceRegistry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static me.hypocrite30.rpc.core.remote.transport.netty.server.NettyRpcServer.PORT;

/**
 * service provider based on Etcd
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/25 21:49
 */
@Slf4j
public class EtcdServiceProvider implements ServiceProvider {

    /**
     * @key: rpc service name
     * @value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;


    public EtcdServiceProvider() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("etcd");
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(hostAddress, PORT));
        } catch (UnknownHostException e) {
            log.error("Error to getLocalHost", e);
        }
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (service == null) {
            throw new RpcException(RpcErrorEnum.NOT_FOUND_SERVICE);
        }
        return service;
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            // service has been registered
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add Service: [{}], implement by interface: [{}]", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }
}
