package me.hypocrite30.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * @Description: Rpc 实现类方法标记注解
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:06
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {
}
