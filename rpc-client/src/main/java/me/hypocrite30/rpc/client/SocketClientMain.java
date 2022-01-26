package me.hypocrite30.rpc.client;

import me.hypocrite30.rpc.api.EchoService;
import me.hypocrite30.rpc.api.Entity;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.proxy.RpcClientProxy;
import me.hypocrite30.rpc.core.remote.transport.RequestTransporter;
import me.hypocrite30.rpc.core.remote.transport.socket.SocketRpcClient;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/7 21:46
 */
public class SocketClientMain {
    public static void main(String[] args) {
        RequestTransporter socketRpcClient = new SocketRpcClient();
        RpcServiceConfig serviceConfig = RpcServiceConfig.builder().group("Group1").version("Version1").build();
        RpcClientProxy proxy = new RpcClientProxy(socketRpcClient, serviceConfig);
        EchoService echoService = proxy.getProxy(EchoService.class);
        String result = echoService.echo(new Entity("message11", "description22"));
        System.out.println(result);
    }
}
