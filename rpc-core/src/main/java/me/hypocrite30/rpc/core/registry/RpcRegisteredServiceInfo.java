package me.hypocrite30.rpc.core.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Rpc registerd service information. One service corresponds to an object
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/21 22:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRegisteredServiceInfo {
    private List<String> servicePath;
}