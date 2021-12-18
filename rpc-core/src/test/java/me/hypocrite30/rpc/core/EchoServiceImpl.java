package me.hypocrite30.rpc.core;

import me.hypocrite30.rpc.core.annotation.RpcService;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/14 21:45
 */
@RpcService(group = "group1", version = "version1")
public class EchoServiceImpl implements EchoService {
    @Override
    public String echo() {
        return "echo() has been called";
    }
}
