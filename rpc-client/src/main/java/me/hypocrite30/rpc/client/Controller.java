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

    @RpcReference(version = "v1", group = "g1")
    private EchoService echoService;

    public void echo() throws InterruptedException {
        String hello = this.echoService.echo(new Entity("message11", "description22"));
        // VM Option: -ea
        assert "Hello description is 222".equals(hello);
        Thread.sleep(10000);
        for (int i = 0; i < 10; i++) {
            System.out.println(echoService.echo(new Entity("message11", "description22")));
        }
    }
}
