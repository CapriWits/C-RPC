package me.hypocrite30.rpc.common.exception;

import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import org.junit.jupiter.api.Test;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/10 12:11
 */
public class RpcExceptionTest {

    /**
     * test rpc excption
     */
    @Test
    public void testRpcException() {
        RpcException rpcServiceName = new RpcException(RpcErrorEnum.SERVICE_CALL_FAILED, "RpcServiceName");
    }
}