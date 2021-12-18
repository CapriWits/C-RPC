package me.hypocrite30.rpc.core.loadbalance.impl;

import me.hypocrite30.rpc.core.loadbalance.AbstractLoadBalance;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Load balance by weighted round robin
 * 1. curWeight += weight
 * 2. result = max(curWeight)
 * 3. max(curWeight) -= totalWeight
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/18 20:46
 */
public class WeightedRoundRobinLoadBalance extends AbstractLoadBalance {

    /**
     * @key: server address
     * @value: number of server address
     */
    private Map<String, Integer> weightMap;
    /**
     * count server address current weight「dynamic weight」
     *
     * @key: server address
     * @value: number of server address
     */
    private static Map<String, Integer> curWeight = new ConcurrentHashMap<>();
    private int totalWeights;

    @Override
    protected String doSelect(List<String> addressList, RpcRequest rpcRequest) {
        initialization(addressList);
        int maxWeight = curWeight.values().stream().mapToInt(w -> w).max().getAsInt();
        String res = "";
        for (Map.Entry<String, Integer> weight : curWeight.entrySet()) {
            // max(curWeight) -= totalWeights
            if (weight.getValue().equals(maxWeight)) {
                res = weight.getKey();
                curWeight.put(weight.getKey(), weight.getValue() - totalWeights);
                break;
            }
        }
        return res;
    }

    private void initialization(List<String> addressList) {
        weightMap = new ConcurrentHashMap<>();
        // calculate number of server address
        for (String ip : addressList) {
            weightMap.put(ip, weightMap.getOrDefault(ip, 0) + 1);
        }
        // curWeight += weight
        for (String ip : addressList) {
            curWeight.put(ip, curWeight.getOrDefault(ip, 0) + 1); // dynamic weight is zero firstly
        }
        totalWeights = weightMap.values().stream().mapToInt(w -> w).sum();
    }
}
