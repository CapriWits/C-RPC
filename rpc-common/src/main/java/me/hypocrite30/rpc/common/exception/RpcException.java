package me.hypocrite30.rpc.common.exception;

import me.hypocrite30.rpc.common.enums.RpcErrorEnum;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/10 11:00
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorEnum errorEnum, String serviceName) {
        super("Error! Service name: [" + serviceName + "], Error Msg: [" + errorEnum.getErrorMsg() + "]");
    }

    public RpcException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
    }

    public RpcException(RpcErrorEnum errorEnum) {
        super(errorEnum.getErrorMsg());
    }
}
