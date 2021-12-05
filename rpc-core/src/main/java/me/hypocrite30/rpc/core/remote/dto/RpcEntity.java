package me.hypocrite30.rpc.core.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rpc Entity
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/25 21:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcEntity {
    // rpc message type
    private byte messageType;
    // serialization type
    private byte codec;
    // compress type
    private byte compressType;
    // request id
    private int requestId;
    // request data
    private Object data;
}
