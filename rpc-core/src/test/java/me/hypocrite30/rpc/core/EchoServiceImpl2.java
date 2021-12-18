package me.hypocrite30.rpc.core;

import me.hypocrite30.rpc.core.annotation.RpcService;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/14 21:45
 */
@RpcService(group = "group2", version = "version2")
public class EchoServiceImpl2 implements EchoService {
    @Override
    public String echo() {
        return "echo2() has been called";
    }
}
