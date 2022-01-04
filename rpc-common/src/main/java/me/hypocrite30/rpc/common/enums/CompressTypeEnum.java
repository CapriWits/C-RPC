package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2022/1/18 21:40
 */
@AllArgsConstructor
@Getter
@ToString
public enum CompressTypeEnum {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum compressTypeEnum : CompressTypeEnum.values()) {
            if (compressTypeEnum.getCode() == code)
                return compressTypeEnum.name;
        }
        return null;
    }
}
