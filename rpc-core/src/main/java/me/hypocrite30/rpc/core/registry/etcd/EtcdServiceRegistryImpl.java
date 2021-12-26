package me.hypocrite30.rpc.core.registry.etcd;

import io.etcd.jetcd.KV;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.core.registry.ServiceRegistry;
import me.hypocrite30.rpc.core.registry.etcd.util.EtcdUtils;

import java.net.InetSocketAddress;

/**
 * service registry based on etcd
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/12 22:27
 */
@Slf4j
public class EtcdServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = EtcdUtils.ETCD_REGISTRY_ROOT + "/" + rpcServiceName;
        KV etcdClient = EtcdUtils.getEtcdClient();
        EtcdUtils.addServiceAddressToEtcd(etcdClient, servicePath, inetSocketAddress);
    }
}
