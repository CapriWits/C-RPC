package me.hypocrite30.rpc.server.impl;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.api.EchoService;
import me.hypocrite30.rpc.api.Entity;
import me.hypocrite30.rpc.core.annotation.RpcService;

/**
 * service implementation
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:03
 */
@Slf4j
@RpcService(group = "g1", version = "v1")
public class EchoServiceImpl implements EchoService {

    static {
        log.info("EchoServiceImpl has been created...");
    }

    @Override
    public String echo(Entity entity) {
        log.info("EchoServiceImpl receive message: {}", entity.getMessage());
        String res = "EchoServiceImpl has handled description: " + entity.getDescription();
        log.info("EchoServiceImpl return message: {}", res);
        return res;
    }
}
