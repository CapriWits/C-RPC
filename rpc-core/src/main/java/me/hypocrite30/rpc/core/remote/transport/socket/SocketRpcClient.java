package me.hypocrite30.rpc.core.remote.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.registry.ServiceDiscovery;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.remote.transport.RequestTransporter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Rpc Client based on JDK Socket
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/7 22:10
 */
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RequestTransporter {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("etcd");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress serverSocketAddress = serviceDiscovery.findService(rpcRequest);
        try {
            Socket socket = new Socket();
            socket.connect(serverSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException(RpcErrorEnum.SERVICE_CALL_FAILED.getErrorMsg(), e);
        }
    }
}
