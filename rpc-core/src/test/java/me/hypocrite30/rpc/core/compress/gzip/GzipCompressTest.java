package me.hypocrite30.rpc.core.compress.gzip;

import me.hypocrite30.rpc.common.extension.ExtensionLoader;
import me.hypocrite30.rpc.core.EchoService;
import me.hypocrite30.rpc.core.EchoServiceImpl;
import me.hypocrite30.rpc.core.compress.Compress;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import me.hypocrite30.rpc.core.serialize.Serializer;
import me.hypocrite30.rpc.core.serialize.protostuff.ProtostuffSerializer;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/18 23:04
 */
public class GzipCompressTest {
    @Test
    public void testGzipCompress() {
        Compress gzipCompress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension("gzip");
        EchoService echoService = new EchoServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group("group1").version("version1").service(echoService).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(echoService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();
        Serializer protostuffSerializer = new ProtostuffSerializer();
        byte[] rpcRequestBytes = protostuffSerializer.serialize(rpcRequest);
        byte[] compressRpcRequestBytes = gzipCompress.compress(rpcRequestBytes);
        System.out.println("compressRpcRequestBytes.length = " + compressRpcRequestBytes.length);
        byte[] decompressRpcRequestBytes = gzipCompress.decompress(compressRpcRequestBytes);
        System.out.println("decompressRpcRequestBytes.length = " + decompressRpcRequestBytes.length);
        assertEquals(rpcRequestBytes.length, decompressRpcRequestBytes.length);
        System.out.println(new String(rpcRequestBytes));
        System.out.println(new String(decompressRpcRequestBytes));
        System.out.println(Arrays.equals(rpcRequestBytes, decompressRpcRequestBytes));
    }
}