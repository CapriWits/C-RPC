package me.hypocrite30.rpc.server.impl;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.api.EchoService;
import me.hypocrite30.rpc.api.Entity;
import me.hypocrite30.rpc.core.annotation.RpcService;

/**
 * service implementation v2
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:03
 */
@Slf4j
public class EchoServiceImpl2 implements EchoService {

    static {
        log.info("EchoServiceImpl2 has been created...");
    }

    @Override
    public String echo(Entity entity) {
        log.info("EchoServiceImpl2 receive message: {}", entity.getMessage());
        String res = "EchoServiceImpl2 has handled description: " + entity.getDescription();
        log.info("EchoServiceImpl2 return message: {}", res);
        return res;
    }
}
