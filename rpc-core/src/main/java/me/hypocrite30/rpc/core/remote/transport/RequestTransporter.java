package me.hypocrite30.rpc.core.remote.transport;

import me.hypocrite30.rpc.common.extension.SPI;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

/**
 * transporter help client send request
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/7 22:03
 */
@SPI
public interface RequestTransporter {
    /**
     * send request to server and get result
     *
     * @param rpcRequest request entity
     * @return result from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
