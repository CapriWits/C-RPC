package me.hypocrite30.test;

import io.etcd.jetcd.kv.PutResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @Author: Hypocrite30
 * @Date: 2021/11/24 22:13
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPutGetTest {
    private static final String KEY = "/abc/foo-" + System.currentTimeMillis();
    private static final String VALUE = "/abc/foo";

    @Test
    @Order(1)
    void put() throws ExecutionException, InterruptedException {
        PutResponse putResponse = new TestPutGet().put(KEY, VALUE);
        assertNotNull(putResponse);
        assertNotNull(putResponse.getHeader());
    }

    @org.junit.jupiter.api.Test
    @Order(2)
    void get() throws ExecutionException, InterruptedException {
        String getResult = new TestPutGet().get(KEY);
        assertEquals(VALUE, getResult);
    }
}