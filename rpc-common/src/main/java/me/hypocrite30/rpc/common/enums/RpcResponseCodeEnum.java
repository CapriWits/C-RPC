package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/10 11:27
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200, "Remote calling is successful"),
    FAIL(500, "Remote calling is fail");

    private final int code;
    private final String message;
}
