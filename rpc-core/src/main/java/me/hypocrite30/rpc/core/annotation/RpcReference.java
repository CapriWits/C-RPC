package me.hypocrite30.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * RpcReference annotation, which autowire impl class
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/5 22:25
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    /**
     * Service version
     */
    String version() default "";

    /**
     * Service group
     */
    String group() default "";
}
