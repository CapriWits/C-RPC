package me.hypocrite30.rpc.core.serialize.protostuff;

import me.hypocrite30.rpc.core.remote.dto.RpcRequest;
import org.junit.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/5 20:53
 */
public class ProtostuffSerializerTest {

    @Test
    public void ProtostuffSerializerTest() {
        RpcRequest src = RpcRequest.builder()
                .methodName("echo")
                .parameters(new Object[]{"paremeter11111111", "paremeter2222222"})
                .interfaceName("me.hypocrite30.rpc.api.EchoService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();

        ProtostuffSerializer kryoSerializer = new ProtostuffSerializer();

        byte[] bytes = kryoSerializer.serialize(src);
        RpcRequest desc = kryoSerializer.deserialize(bytes, RpcRequest.class);

        assertEquals(src.getGroup(), desc.getGroup());
        assertEquals(src.getVersion(), desc.getVersion());
        assertEquals(src.getRequestId(), desc.getRequestId());
    }
}