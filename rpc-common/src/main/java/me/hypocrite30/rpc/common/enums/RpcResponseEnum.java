package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2021/11/20 21:31
 */
@Getter
@AllArgsConstructor
@ToString
public enum RpcResponseEnum {
    SUCCESS(200, "Rpc response successfully"),
    FAIL(500, "Rpc response is fail");

    private final int code;
    private final String message;
}
