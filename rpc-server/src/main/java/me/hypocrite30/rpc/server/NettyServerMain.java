package me.hypocrite30.rpc.server;

import me.hypocrite30.rpc.core.annotation.RpcScan;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.remote.transport.netty.server.NettyRpcServer;
import me.hypocrite30.rpc.server.impl.EchoServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * register service automatic by scan @RpcService
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/7 22:32
 */
@RpcScan(basePackage = {"me.hypocrite30"})
public class NettyServerMain {
    public static void main(String[] args) {
        // register service by scan @RpcService
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // register service manually
        EchoServiceImpl2 service = new EchoServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group("g2").version("v2").service(service).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
