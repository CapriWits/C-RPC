package me.hypocrite30.rpc.common.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 单例工厂
 * @Author: Hypocrite30
 * @Date: 2021/11/20 20:07
 */
public class SingletonFactory {
    private static final Map<String, Object> SINGLETON_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    /**
     * 获取单例
     * 不存在，则反射获取无参构造器，new 实例，存入单例 Map，返回结果
     * 返回结果都需要 cast 强转
     */
    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (SINGLETON_MAP.containsKey(key)) {
            return c.cast(SINGLETON_MAP.get(key)); // cast 强转类型
        } else {
            return c.cast(SINGLETON_MAP.computeIfAbsent(key, k -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException();
                }
            }));
        }
    }
}
