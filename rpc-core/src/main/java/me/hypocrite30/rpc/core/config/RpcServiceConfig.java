package me.hypocrite30.rpc.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Hypocrite30
 * @Date: 2021/11/20 22:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcServiceConfig {
    // service provider object
    private Object service;
    // distinguish different implementations by the same interface
    private String group = "";
    // mark service version
    private String version = "";

    public String getRpcServiceName() {
        return this.getServiceName() + "#" + this.getGroup() + "#" + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
