package me.hypocrite30.rpc.server;

import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.remote.transport.socket.SocketRpcServer;
import me.hypocrite30.rpc.server.impl.EchoServiceImpl;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/8 22:51
 */
public class SocketServerMain {
    public static void main(String[] args) {
        EchoServiceImpl echoService = new EchoServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig serviceConfig = RpcServiceConfig.builder().group("Group1").version("Version1").service(echoService).build();
        socketRpcServer.registerService(serviceConfig);
        socketRpcServer.start();
    }
}
