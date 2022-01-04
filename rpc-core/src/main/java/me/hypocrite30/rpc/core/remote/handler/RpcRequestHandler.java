package me.hypocrite30.rpc.core.remote.handler;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.core.provider.ServiceProvider;
import me.hypocrite30.rpc.core.provider.impl.EtcdServiceProvider;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Rpc server resquest handler
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/16 23:28
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(EtcdServiceProvider.class);
    }

    /**
     * To handler client request
     *
     * @param rpcRequest client request
     * @return invoke result
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeMethod(rpcRequest, service);
    }

    /**
     * invoke service method
     *
     * @param rpcRequest client request
     * @param service    service object
     * @return invoke result
     */
    private Object invokeMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("Service: [{}] has invoked method: [{}] successfully", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException("Server invoke method failed", e);
        }
        return result;
    }
}
