package me.hypocrite30.rpc.core.loadbalance.impl;

import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.EchoService;
import me.hypocrite30.rpc.core.EchoServiceImpl;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.loadbalance.LoadBalance;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/18 22:28
 */
public class ConsistentHashLoadBalanceTest {
    @Test
    public void testConsistentHashLoadBalance() {
        List<String> severAddress = new ArrayList<>(
                Arrays.asList("127.0.0.5", "127.0.0.2", "127.0.0.3", "127.0.0.2", "127.0.0.3",
                        "127.0.0.5", "127.0.0.3", "127.0.0.5", "127.0.0.5", "127.0.0.5"));
        LoadBalance consistenthash = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("consistenthash");
        EchoService echoService = new EchoServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group("group1").version("version1").service(echoService).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(echoService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        for (int i = 0; i < 10; i++) {
            String serverIP = consistenthash.selectServerAddress(severAddress, rpcRequest);
            System.out.println(serverIP);
        }
    }
}