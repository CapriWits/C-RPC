package me.hypocrite30.rpc.common.utils.concurrent.threadpool;

import org.junit.jupiter.api.Test;

/**
 * Test for ThreadPoolUtils
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/18 22:15
 */
public class ThreadPoolUtilsTest {

    /**
     * 测试线程工厂是否按照 prefix 创建ThreadFactory
     */
    @Test
    public void testCreateThreadFactory() {
        ThreadPoolUtils.createThreadFactory("socket-server-pool", false);
    }
}