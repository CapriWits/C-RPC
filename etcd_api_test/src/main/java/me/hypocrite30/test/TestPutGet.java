package me.hypocrite30.test;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Description: Test for Etcd Put & Get
 * @Author: Hypocrite30
 * @Date: 2021/11/24 21:19
 */
@Slf4j
public class TestPutGet {

    private KV getKVClient() {
        String endpoints = "http://192.168.248.128:2379,http://192.168.248.128:2380,http://192.168.248.128:2381";
        Client client = Client.builder().endpoints(endpoints.split(",")).build();
        return client.getKVClient();
    }

    /**
     * 将字符串转为客户端所需的 ByteSequence 实例
     */
    private static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, UTF_8);
    }

    /**
     * 查询指定键对应的值
     */
    public String get(String key) throws ExecutionException, InterruptedException {
        log.info("start get, key [{}]", key);
        GetResponse response = getKVClient().get(bytesOf(key)).get();
        if (response.getKvs().isEmpty()) {
            log.error("empty value of key [{}]", key);
            return null;
        }
        String value = response.getKvs().get(0).getValue().toString(UTF_8);
        log.info("finish get, key [{}], value [{}]", key, value);
        return value;
    }

    /**
     * 创建键值对
     * @return
     */
    public PutResponse put(String key, String value) throws ExecutionException, InterruptedException {
        log.info("start put, key [{}], value [{}]", key, value);
        return getKVClient().put(bytesOf(key), bytesOf(value)).get();
    }
}
