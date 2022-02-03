package me.hypocrite30.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * RpcService annotation, which mark the impl class
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:06
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {
    /**
     * Service version
     */
    String version() default "";

    /**
     * Service group
     */
    String group() default "";
}