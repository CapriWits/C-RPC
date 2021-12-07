package me.hypocrite30.rpc.core.spring;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.core.annotation.RpcScan;
import me.hypocrite30.rpc.core.annotation.RpcService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * scan and filter specified annotations
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/5 22:28
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    public static final String BEAN_BASE_PACKAGE = "me.hypocrite30";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Firstly scan class which has @RpcScan to get all Rpc class
     * Then scan annotated class with @me.hypocrite30.rpc.core.annotation.RpcService | @org.springframework.stereotype.Component
     *
     * @param annotationMetadata     to get annotation meta data
     * @param beanDefinitionRegistry to register bean
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // get all annotations's attributes and value, store as LinkedHashMap<String, Object>
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // get the value of the basePackage attribute
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            // if have not set basePackage scan path, just scan the value under the corresponding package
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // Scan the RpcService annotation
        MyClassPathBeanScanner rpcServiceScanner = new MyClassPathBeanScanner(beanDefinitionRegistry, RpcService.class);
        // Scan the Component annotation
        MyClassPathBeanScanner springBeanScanner = new MyClassPathBeanScanner(beanDefinitionRegistry, Component.class);
        // put ResourceLoader into all scanner
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        // scan by ClassPathBeanDefinitionScanner
        int springBeanAmount = springBeanScanner.scan(BEAN_BASE_PACKAGE);
        log.info("spring bean amount: [{}]", springBeanAmount);
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpc service amount: [{}]", rpcServiceCount);
    }
}
