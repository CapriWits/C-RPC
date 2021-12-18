package me.hypocrite30.rpc.core.loadbalance.impl;

import me.hypocrite30.rpc.core.loadbalance.AbstractLoadBalance;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Load balance by round robin
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/16 21:42
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    /**
     * @key: server address
     * @value: number of server address
     */
    private Map<String, Integer> weightMap;
    private int totalWeights;
    private static int index = 0;

    @Override
    protected String doSelect(List<String> addressList, RpcRequest rpcRequest) {
        initWeightMap(addressList);
        if (index == addressList.size()) {
            index = 0;
        }
        int pos = (index++) % totalWeights;
        for (String ip : weightMap.keySet()) {
            Integer curWeight = weightMap.get(ip);
            if (pos < curWeight) {
                return ip;
            }
            pos = pos - curWeight;
        }
        return "";
    }

    /**
     * count number of server address, calculate total weights in address list
     */
    private void initWeightMap(List<String> addressList) {
        weightMap = new ConcurrentHashMap<>();
        addressList.stream().forEach(ip -> weightMap.put(ip, weightMap.getOrDefault(ip, 0) + 1));
        totalWeights = weightMap.values().stream().mapToInt(w -> w).sum();
    }
}
