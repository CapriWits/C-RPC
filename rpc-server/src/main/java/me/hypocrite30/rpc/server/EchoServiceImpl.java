package me.hypocrite30.rpc.server;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.api.EchoService;
import me.hypocrite30.rpc.api.Entity;
import me.hypocrite30.rpc.core.annotation.RpcService;

/**
 * @Description: 服务方法实现类
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:03
 */
@Slf4j
@RpcService
public class EchoServiceImpl implements EchoService {
    @Override
    public String testMethod(Entity entity) {
        log.info("EchoServiceImpl receive message: {}", entity.getMessage());
        String res = "EchoServiceImpl has handled description: " + entity.getDescription();
        log.info("EchoServiceImpl return message: {}", res);
        return res;
    }
}
