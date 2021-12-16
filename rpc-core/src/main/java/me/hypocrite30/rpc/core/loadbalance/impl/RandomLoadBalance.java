package me.hypocrite30.rpc.core.loadbalance.impl;

import me.hypocrite30.rpc.core.loadbalance.AbstractLoadBalance;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * Load balance by random selection without weight
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/16 21:09
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> addressList, RpcRequest rpcRequest) {
        return addressList.get(new Random().nextInt(addressList.size()));
    }
}
