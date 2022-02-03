package me.hypocrite30.rpc.core.annotation;

import me.hypocrite30.rpc.core.spring.RpcServiceScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * RpcScan annotation, which scan custom annotations
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/5 22:27
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServiceScannerRegistrar.class) // import registrar to achieve custom scanner
public @interface RpcScan {

    String[] basePackage() default {};

}