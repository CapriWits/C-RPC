package me.hypocrite30.rpc.common.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description: 线程池工具类
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:33
 */
@Slf4j
public class ThreadPoolUtils {

    /**
     * 给线程池命名前缀，区分线程池
     * @Key: ThreadNamePrefix
     * @Value: ThreadPool
     */
    private static final Map<String, ExecutorService> POOLS = new ConcurrentHashMap<>();

    private ThreadPoolUtils() {
    }

    public static ExecutorService createThreadPoolIfAbsent(String threadNamePrefix) {
        return createThreadPoolIfAbsent(new ThreadPoolConfig(), threadNamePrefix, false);
    }

    /**
     * 命名前缀对应线程池若存在，直接返回。否则创建新的映射关系记录，并返回新线程池
     * @param threadPoolConfig 线程池配置类
     * @param threadNamePrefix 线程池命名前缀
     * @param daemon           是否为守护线程
     * @return ExecutorService
     */
    public static ExecutorService createThreadPoolIfAbsent(ThreadPoolConfig threadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ExecutorService threadPool = POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadPoolConfig, threadNamePrefix, daemon));
        // 被 shutdown | terminate，则手动创建
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(threadPoolConfig, threadNamePrefix, daemon);
            POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    public static ExecutorService createThreadPool(ThreadPoolConfig threadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getUnit(),
                threadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 如果有自定义命名线程前缀，自定义创建 ThreadFactory，否则使用 defaultThreadFactory
     * @param threadNamePrefix 线程命名前缀，用于区分线程
     * @param daemon           是否为守护线程
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) { // daemon 要判空，用包装类型
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool key: [{}] isTerminated: [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool has not terminated yet");
                executorService.shutdownNow();
            }
        });
    }
}
