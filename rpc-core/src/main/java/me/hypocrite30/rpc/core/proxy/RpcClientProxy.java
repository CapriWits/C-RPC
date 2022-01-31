package me.hypocrite30.rpc.core.proxy;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.enums.RpcResponseCodeEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.registry.etcd.EtcdScheduledUpdater;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.remote.dto.RpcResponse;
import me.hypocrite30.rpc.core.remote.transport.RequestTransporter;
import me.hypocrite30.rpc.core.remote.transport.netty.client.NettyRpcClient;
import me.hypocrite30.rpc.core.remote.transport.socket.SocketRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Client dynamic proxy class
 * The Proxy class implements InvocationHandler so that when Proxy.newProxyInstance will invoke following [invoke] method
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/8 20:19
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RequestTransporter requestTransporter;
    private final RpcServiceConfig rpcServiceConfig;
    private final EtcdScheduledUpdater etcdScheduledUpdater;

    public RpcClientProxy(RequestTransporter requestTransporter, RpcServiceConfig rpcServiceConfig) {
        this.requestTransporter = requestTransporter;
        this.rpcServiceConfig = rpcServiceConfig;
        etcdScheduledUpdater = new EtcdScheduledUpdater();
        etcdScheduledUpdater.run();
        etcdScheduledUpdater.start(0, 30);
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * Method [getProxy] invoke this method to proxy an object
     *
     * @param proxy  proxy object
     * @param method invoked method
     * @param args   parameters
     * @return invoke result
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException {
        log.info("Method [{}] has been invoked", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (requestTransporter instanceof NettyRpcClient) {
            // CompletableFuture to get return result asynchronously
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) requestTransporter.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        if (requestTransporter instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse<Object>) requestTransporter.sendRpcRequest(rpcRequest);
        }
        CheckResponse(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void CheckResponse(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorEnum.SERVICE_CALL_FAILED, rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorEnum.REQUEST_NOT_MATCH_RESPONSE, rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorEnum.SERVICE_CALL_FAILED, rpcRequest.getInterfaceName());
        }
    }
}