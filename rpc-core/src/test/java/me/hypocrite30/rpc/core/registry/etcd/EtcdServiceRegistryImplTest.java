package me.hypocrite30.rpc.core.registry.etcd;

import me.hypocrite30.rpc.core.EchoService;
import me.hypocrite30.rpc.core.EchoServiceImpl;
import me.hypocrite30.rpc.core.EchoServiceImpl2;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.registry.ServiceRegistry;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/13 20:30
 */
public class EtcdServiceRegistryImplTest {
    @Test
    public void testRegisterTheSameServiceForTwice() {
        ServiceRegistry etcdServiceRegistry = new EtcdServiceRegistryImpl();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9996);
        EchoService echoService = new EchoServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("group1").version("version1").service(echoService).build();
        etcdServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress);
        // register for the same service again
        etcdServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress);
    }

    @Test
    public void testRegisterDifferentService() {
        ServiceRegistry etcdServiceRegistry = new EtcdServiceRegistryImpl();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9996);
        EchoService echoService = new EchoServiceImpl();
        EchoService echoService2 = new EchoServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("group1").version("version1").service(echoService).build();
        RpcServiceConfig rpcServiceConfig2 = RpcServiceConfig.builder()
                .group("group2").version("version2").service(echoService2).build();
        etcdServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress);
        etcdServiceRegistry.registerService(rpcServiceConfig2.getRpcServiceName(), inetSocketAddress);
    }

    @Test
    public void testRegisterTheSameServiceWithDifferentAddress() {
        ServiceRegistry etcdServiceRegistry = new EtcdServiceRegistryImpl();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 1111);
        InetSocketAddress inetSocketAddress2 = new InetSocketAddress("127.0.0.2", 2222);
        EchoService echoService = new EchoServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("group1").version("version1").service(echoService).build();
        etcdServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress);
        etcdServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress2);
    }
}