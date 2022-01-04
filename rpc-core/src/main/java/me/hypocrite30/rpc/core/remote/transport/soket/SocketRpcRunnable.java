package me.hypocrite30.rpc.core.remote.transport.soket;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.remote.dto.RpcResponse;
import me.hypocrite30.rpc.core.remote.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * connect with client and execute business
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/20 20:00
 */
@Slf4j
public class SocketRpcRunnable implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("SocketRpcServer handler massage by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            outputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            outputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Socket runnable occur IOException: ", e);
        }
    }
}
