package me.hypocrite30.rpc.core.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hypocrite30.rpc.common.enums.RpcResponseEnum;

import java.io.Serializable;

/**
 * @Description: RPC 响应消息体
 * @Author: Hypocrite30
 * @Date: 2021/11/20 21:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder // 建造者为内部类，自动生成
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 89594398918299851L;
    private Integer code;
    private String message;
    private T data;
    private String requestId;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        // RpcResponse<T> response = new RpcResponse<>();
        // response.setCode(RpcResponseEnum.SUCCESS.getCode());
        // response.setMessage(RpcResponseEnum.SUCCESS.getMessage());
        // response.setRequestId(requestId);
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
