package me.hypocrite30.rpc.core.remote.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.CompressTypeEnum;
import me.hypocrite30.rpc.common.enums.SerializationTypeEnum;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.core.remote.dto.RpcEntity;
import me.hypocrite30.rpc.core.remote.dto.RpcResponse;
import me.hypocrite30.rpc.core.remote.transport.netty.codec.RpcCodecConstants;

import java.net.InetSocketAddress;

/**
 * netty client handle rpc response from netty server
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/21 10:45
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcEntity) {
                RpcEntity tmp = (RpcEntity) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcCodecConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcCodecConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("writer idle happen [{}], PING to the server", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcEntity rpcEntity = new RpcEntity();
                rpcEntity.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcEntity.setCompressType(CompressTypeEnum.GZIP.getCode());
                rpcEntity.setMessageType(RpcCodecConstants.HEARTBEAT_REQUEST_TYPE);
                rpcEntity.setData(RpcCodecConstants.PING);
                channel.writeAndFlush(rpcEntity).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
