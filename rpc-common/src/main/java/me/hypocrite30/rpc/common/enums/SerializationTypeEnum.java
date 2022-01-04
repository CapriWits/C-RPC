package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2022/1/18 21:51
 */
@AllArgsConstructor
@Getter
@ToString
public enum SerializationTypeEnum {

    PROTOSTUFF((byte) 0x01, "protostuff");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum serializationTypeEnum : SerializationTypeEnum.values()) {
            if (serializationTypeEnum.getCode() == code)
                return serializationTypeEnum.name;
        }
        return null;
    }
}
