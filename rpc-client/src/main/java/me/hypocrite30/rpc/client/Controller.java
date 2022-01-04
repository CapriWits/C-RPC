package me.hypocrite30.rpc.client;

import me.hypocrite30.rpc.api.EchoService;
import me.hypocrite30.rpc.api.Entity;
import me.hypocrite30.rpc.core.annotation.RpcReference;
import org.springframework.stereotype.Component;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/5 22:21
 */
@Component
public class Controller {

    // to scan @RpcService for registering
    @RpcReference(version = "v1", group = "g1")
    private EchoService echoService;

    // register rpc service manually
    @RpcReference(version = "v2", group = "g2")
    private EchoService echoService2;

    public void echo() {
        for (int i = 0; i < 5; i++) {
            System.out.println(echoService.echo(new Entity("message11", "description22")));
            System.out.println(echoService2.echo(new Entity("nessage22", "description22")));
        }
    }
}
