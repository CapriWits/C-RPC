package me.hypocrite30.rpc.core.remote.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.CompressTypeEnum;
import me.hypocrite30.rpc.common.enums.SerializationTypeEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.compress.Compress;
import me.hypocrite30.rpc.core.remote.dto.RpcEntity;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.remote.dto.RpcResponse;
import me.hypocrite30.rpc.core.serialize.Serializer;

import java.util.Arrays;

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
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/18 22:19
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * lengthFieldOffset: magic code: 4B & version: 1B & full length. so value is 5
     * lengthFieldLength: full length: 4B. so value is 4
     * lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
     * initialBytesToStrip: check magic code and version manually, do not strip any bytes. so values is 0
     */
    public RpcMessageDecoder() {
        this(RpcCodecConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. Discard message if length of data is exceeds.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of th length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            Value equals 0 if you need receive all the header+body message.
     *                            If you want to receive message body, you need to skip number of bytes consumed by header.
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcCodecConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * Decode message frame to RpcEntity object
     *
     * @param in ByteBuf
     * @return RpcEntity Object
     */
    private Object decodeFrame(ByteBuf in) {
        // check Magic Number & version
        checkMagicNumber(in);
        checkVersion(in);
        final int fullLength = in.readInt();
        // build RpcEntity object
        final byte messageType = in.readByte();
        final byte codecType = in.readByte();
        final byte compressType = in.readByte();
        final int requestId = in.readInt();
        RpcEntity rpcEntity = RpcEntity.builder()
                .messageType(messageType)
                .codec(codecType)
                .requestId(requestId).build();
        // message type is heart beat, send PING or PONG directly
        if (messageType == RpcCodecConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcEntity.setData(RpcCodecConstants.PING);
            return rpcEntity;
        }
        if (messageType == RpcCodecConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcEntity.setData(RpcCodecConstants.PONG);
            return rpcEntity;
        }
        int bodyLength = fullLength - RpcCodecConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            // decompress the bytes
            String compressName = CompressTypeEnum.getName(compressType);
            log.info("compress name: [{}] ", compressName);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            body = compress.decompress(body);
            // deserialize the object
            String codecName = SerializationTypeEnum.getName(codecType);
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if (messageType == RpcCodecConstants.REQUEST_TYPE) {
                RpcRequest data = serializer.deserialize(body, RpcRequest.class);
                rpcEntity.setData(data);
            } else {
                RpcResponse data = serializer.deserialize(body, RpcResponse.class);
                rpcEntity.setData(data);
            }
        }
        return rpcEntity;
    }

    /**
     * To check message header's version
     *
     * @param in ByteBuf
     */
    private void checkVersion(ByteBuf in) {
        // read 1 bit version
        byte version = in.readByte();
        if (version != RpcCodecConstants.VERSION) {
            throw new RpcException("version mismatch: " + version);
        }
        log.info("Version check pass");
    }

    /**
     * To check message header's Magic Number
     *
     * @param in ByteBuf
     */
    private void checkMagicNumber(ByteBuf in) {
        int len = RpcCodecConstants.MAGIC_NUM.length;
        byte[] tmp = new byte[len];
        // read 4 bits Magic Number
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcCodecConstants.MAGIC_NUM[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
        log.info("Magic Number check pass");
    }
}
