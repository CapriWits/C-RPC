package me.hypocrite30.rpc.client;

import me.hypocrite30.rpc.core.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: Hypocrite30
 * @Date: 2022/1/20 15:36
 */
@RpcScan(basePackage = {"me.hypocrite30"})
public class NettyClientMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        Controller controller = ((Controller) applicationContext.getBean("controller"));
        controller.echo();
    }
}