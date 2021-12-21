package me.hypocrite30.rpc.core.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Rpc server information
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/21 22:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcServerInfo implements Serializable {
    private static final long serialVersionUID = 4443004013026142311L;
    private String rpcServiceName;
    private List<String> IP;
}
