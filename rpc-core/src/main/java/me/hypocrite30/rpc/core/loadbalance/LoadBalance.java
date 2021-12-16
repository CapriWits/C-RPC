package me.hypocrite30.rpc.core.loadbalance;

import me.hypocrite30.rpc.common.extension.SPI;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.util.List;

/**
 * Load balance policy
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/16 20:59
 */
@SPI
public interface LoadBalance {
    /**
     * select a available server address
     *
     * @param addressList available server list
     * @param rpcRequest  rpc request
     * @return server address
     */
    String selectServerAddress(List<String> addressList, RpcRequest rpcRequest);
}
