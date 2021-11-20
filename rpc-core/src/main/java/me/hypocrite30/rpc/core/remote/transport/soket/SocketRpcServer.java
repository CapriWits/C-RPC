package me.hypocrite30.rpc.core.remote.transport.soket;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.utils.concurrent.threadpool.ThreadPoolUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static me.hypocrite30.rpc.core.remote.transport.netty.server.NettyServer.PORT;

/**
 * @Description: 原生 JDK 实现
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:26
 */
@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;

    public SocketRpcServer() {
        threadPool = ThreadPoolUtils.createThreadPoolIfAbsent("socket-server-pool");
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(hostAddress, PORT));
            Socket socket;
            // 监听消息
            while ((socket = serverSocket.accept()) != null) {
                log.info("Client has connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("IOException: [{}]", e);
        }
    }
}
