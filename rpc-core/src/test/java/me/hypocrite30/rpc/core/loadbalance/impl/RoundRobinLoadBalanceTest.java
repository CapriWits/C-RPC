package me.hypocrite30.rpc.core.loadbalance.impl;

import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.loadbalance.LoadBalance;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/16 22:43
 */
public class RoundRobinLoadBalanceTest {
    @Test
    public void testRoundRobinLoadBalance() {
        List<String> severAddress = new ArrayList<>(
                Arrays.asList("127.0.0.5", "127.0.0.2", "127.0.0.3", "127.0.0.2", "127.0.0.3",
                        "127.0.0.5", "127.0.0.3", "127.0.0.5", "127.0.0.5", "127.0.0.5"));
        LoadBalance roundRobinLoadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("roundrobin");
        for (int i = 0; i < 10; i++) {
            String serverIP = roundRobinLoadBalance.selectServerAddress(severAddress, new RpcRequest());
            System.out.println(serverIP);
        }
    }
}