package me.hypocrite30.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/13 22:20
 */
@Getter
@AllArgsConstructor
@ToString
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ETCD_ENDPOINTS("crpc.etcd.endpoints");

    private final String value;
}
