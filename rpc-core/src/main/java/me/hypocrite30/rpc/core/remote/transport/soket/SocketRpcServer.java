package me.hypocrite30.rpc.core.remote.transport.soket;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.common.utils.concurrent.threadpool.ThreadPoolUtils;
import me.hypocrite30.rpc.common.utils.net.NetUtils;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.provider.ServiceProvider;
import me.hypocrite30.rpc.core.provider.impl.EtcdServiceProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static me.hypocrite30.rpc.core.remote.transport.netty.server.NettyRpcServer.PORT;

/**
 * RpcServer based on JDK Socket
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:26
 */
@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        threadPool = ThreadPoolUtils.createThreadPoolIfAbsent("socket-rpc-server-pool");
        serviceProvider = SingletonFactory.getInstance(EtcdServiceProvider.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            String host = NetUtils.getLocalHostExactAddress().toString();
            serverSocket.bind(NetUtils.newInetSocketAddress(host + ":" + PORT));
            // log.info("Server socket has bind IP: [{}:{}]", ip, PORT);
            Socket socket;
            // Listen for messages
            while ((socket = serverSocket.accept()) != null) {
                log.info("Client [{}:{}] has connected", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRpcRunnable(socket));
            }
        } catch (IOException e) {
            log.error("IOException: ", e);
        } finally {
            threadPool.shutdown();
        }
    }
}
