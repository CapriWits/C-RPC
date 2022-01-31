package me.hypocrite30.rpc.core.registry.etcd;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.common.utils.net.NetUtils;
import me.hypocrite30.rpc.core.loadbalance.LoadBalance;
import me.hypocrite30.rpc.core.registry.ServiceDiscovery;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * service discovery based on etcd
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/12 22:20
 */
@Slf4j
public class EtcdServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public EtcdServiceDiscoveryImpl() {
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("consistenthash");
    }

    @Override
    public InetSocketAddress findService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        List<String> servicesHostList = EtcdScheduledUpdater.registeredServices.get(rpcServiceName);
        if (CollectionUtils.isEmpty(servicesHostList)) {
            throw new RpcException(RpcErrorEnum.NOT_FOUND_SERVICE, rpcServiceName);
        }
        String selectServerAddress = loadBalance.selectServerAddress(servicesHostList, rpcRequest);
        log.info("Find service successfully, the address: [{}]", selectServerAddress);
        return NetUtils.newInetSocketAddress(selectServerAddress);
    }
}