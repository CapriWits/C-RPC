package me.hypocrite30.rpc.core.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hypocrite30.rpc.common.enums.RpcResponseEnum;

import java.io.Serializable;

/**
 * RPC response entity
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/20 21:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 89594398918299851L;
    private String requestId;
    // response code
    private Integer code;
    // response message
    private String message;
    // response body
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponseBuilder builder = new RpcResponseBuilder()
                .code(RpcResponseEnum.SUCCESS.getCode())
                .message(RpcResponseEnum.SUCCESS.getMessage())
                .requestId(requestId);
        RpcResponse response = builder.build();
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
