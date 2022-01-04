package me.hypocrite30.rpc.core.remote.transport.netty.server;

import  io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.CompressTypeEnum;
import me.hypocrite30.rpc.common.enums.RpcResponseEnum;
import me.hypocrite30.rpc.common.enums.SerializationTypeEnum;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.core.remote.dto.RpcEntity;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.remote.dto.RpcResponse;
import me.hypocrite30.rpc.core.remote.handler.RpcRequestHandler;
import me.hypocrite30.rpc.core.remote.transport.netty.codec.RpcCodecConstants;

/**
 * netty server handle rpc request asynchronously
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/18 22:49
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcEntity) {
                log.info("server receive message: [{}]", msg);
                byte messageType = ((RpcEntity) msg).getMessageType();
                RpcEntity rpcEntity = new RpcEntity();
                rpcEntity.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcEntity.setCompressType(CompressTypeEnum.GZIP.getCode());
                // if message is to check heart beat, send back 'PONG'
                if (messageType == RpcCodecConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcEntity.setMessageType(RpcCodecConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcEntity.setData(RpcCodecConstants.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcEntity) msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("The return result which server has handled: [{}]", result.toString());
                    rpcEntity.setMessageType(RpcCodecConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcEntity.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseEnum.FAIL);
                        rpcEntity.setData(rpcResponse);
                        log.error("write response message error, message has dropped");
                    }
                }
                ctx.writeAndFlush(rpcEntity).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // ensure message buffer is released, or it will occur memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("reader idle check happen, close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
