package me.hypocrite30.rpc.core.serialize;

import me.hypocrite30.rpc.common.extension.SPI;

/**
 * Serializer interface
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/5 17:28
 */
@SPI
public interface Serializer {
    /**
     * serialize
     *
     * @param obj will be serialized object
     * @return byte array
     */
    byte[] serialize(Object obj);

    /**
     * deserialize
     *
     * @param bytes byte array
     * @param clazz target object class type
     * @param <T>   object type
     * @return object
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
