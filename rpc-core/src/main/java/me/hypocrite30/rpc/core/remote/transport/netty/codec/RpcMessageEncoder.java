package me.hypocrite30.rpc.core.remote.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.CompressTypeEnum;
import me.hypocrite30.rpc.common.enums.SerializationTypeEnum;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.compress.Compress;
import me.hypocrite30.rpc.core.remote.dto.RpcEntity;
import me.hypocrite30.rpc.core.serialize.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 * |   magic   code        |version | full length         | messageType| codec|compress|    RequestId      |
 * +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 * |                                                                                                       |
 * |                                         body                                                          |
 * |                                                                                                       |
 * |                                        ... ...                                                        |
 * +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/18 21:18
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcEntity> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcEntity rpcEntity, ByteBuf byteBuf) {
        try {
            byteBuf.writeBytes(RpcCodecConstants.MAGIC_NUM);
            byteBuf.writeByte(RpcCodecConstants.VERSION);
            // skip "full length" position because it needs to calculate body buffer's length
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcEntity.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(rpcEntity.getCodec());
            byteBuf.writeByte(CompressTypeEnum.GZIP.getCode());
            byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement());
            byte[] bodyBuffer = null;
            int fullLength = RpcCodecConstants.HEAD_LENGTH;
            // if message type is not heartbeat message, full length = head length + body length
            if (messageType != RpcCodecConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcCodecConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(rpcEntity.getCodec());
                log.info("codec name: [{}]", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                bodyBuffer = serializer.serialize(rpcEntity.getData());
                String compressType = CompressTypeEnum.getName(rpcEntity.getCompressType());
                log.info("compress type: [{}]", compressType);
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressType);
                bodyBuffer = compress.compress(bodyBuffer);
                fullLength += bodyBuffer.length;
            }
            if (bodyBuffer != null) {
                byteBuf.writeBytes(bodyBuffer);
            }
            int writeIndex = byteBuf.writerIndex();
            // back to "full length" position
            byteBuf.writerIndex(writeIndex - fullLength + RpcCodecConstants.MAGIC_NUM.length + 1);
            byteBuf.writeInt(fullLength);
            // jump to tail position
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error", e);
        }
    }
}
