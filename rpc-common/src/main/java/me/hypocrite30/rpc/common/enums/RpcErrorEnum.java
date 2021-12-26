package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/10 11:01
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorEnum {
    SERVICE_CALL_FAILED("Service call failed"),
    NOT_FOUND_SERVICE("Not found service"),
    REQUEST_NOT_MATCH_RESPONSE("Request not match response"),
    FAIL_TO_GET_PROPERTIES("Fail to get properties"),
    JSON_FORMAT_ERROR("etcd value is not json format");

    private final String errorMsg;
}
