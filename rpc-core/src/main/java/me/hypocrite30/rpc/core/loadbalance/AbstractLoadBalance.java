package me.hypocrite30.rpc.core.loadbalance;

import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/16 21:03
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServerAddress(List<String> addressList, RpcRequest rpcRequest) {
        if (CollectionUtils.isEmpty(addressList)) {
            return null;
        }
        if (addressList.size() == 1) {
            return addressList.get(0);
        }
        return doSelect(addressList, rpcRequest);
    }

    protected abstract String doSelect(List<String> addressList, RpcRequest rpcRequest);
}
