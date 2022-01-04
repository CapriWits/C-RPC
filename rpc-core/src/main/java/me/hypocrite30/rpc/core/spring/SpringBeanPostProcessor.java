package me.hypocrite30.rpc.core.spring;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.core.annotation.RpcReference;
import me.hypocrite30.rpc.core.annotation.RpcService;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.provider.ServiceProvider;
import me.hypocrite30.rpc.core.provider.impl.EtcdServiceProvider;
import me.hypocrite30.rpc.core.proxy.RpcClientProxy;
import me.hypocrite30.rpc.core.remote.transport.RequestTransporter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author: Hypocrite30
 * @Date: 2022/1/21 16:21
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RequestTransporter rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(EtcdServiceProvider.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RequestTransporter.class).getExtension("netty");
    }

    /**
     * get service bean with @RpcService and publish it
     *
     * @param bean     service bean
     * @param beanName bean name
     * @return spring bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get @RpcService
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build service config with service bean
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     *
     *
     * @param bean     service bean
     * @param beanName bean name
     * @return spring bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClz = bean.getClass();
        Field[] declaredFields = targetClz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    log.error("IllegalAccessException: ", e);
                }
            }
        }
        return bean;
    }
}
