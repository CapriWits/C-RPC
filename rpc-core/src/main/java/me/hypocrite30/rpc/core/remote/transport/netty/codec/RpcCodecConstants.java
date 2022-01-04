package me.hypocrite30.rpc.core.remote.transport.netty.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Some constants used for netty codec
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/18 21:25
 */
public class RpcCodecConstants {

    public static final byte[] MAGIC_NUM = {(byte) 'c', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    // ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    // pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
}
