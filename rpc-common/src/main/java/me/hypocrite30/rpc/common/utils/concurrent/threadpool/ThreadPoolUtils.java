package me.hypocrite30.rpc.common.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @Author: Hypocrite30
 * @Date: 2021/11/17 22:33
 */
@Slf4j
public class ThreadPoolUtils {

    /**
     * Name the thread pool with a prefix to distinguish the thread pool
     *
     * @key: Thread name prefix
     * @value: ThreadPool
     */
    private static final Map<String, ExecutorService> POOLS = new ConcurrentHashMap<>();

    private ThreadPoolUtils() {
    }

    public static ExecutorService createThreadPoolIfAbsent(String threadNamePrefix) {
        return createThreadPoolIfAbsent(new ThreadPoolConfig(), threadNamePrefix, true);
    }

    /**
     * create not repeating thread pool
     *
     * @param threadPoolConfig thread pool config
     * @param threadNamePrefix thread name prefix
     * @param daemon           is daemon thread
     * @return ExecutorService
     */
    public static ExecutorService createThreadPoolIfAbsent(ThreadPoolConfig threadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ExecutorService threadPool = POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadPoolConfig, threadNamePrefix, daemon));
        // manually create thread pool if it is shutdown or terminal
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
     * create scheduled thread pool to finish scheduled task
     *
     * @param threadNamePrefix thread name prefix
     * @param daemon           is daemon thread
     * @return Scheduled thread pool
     */
    public static ScheduledExecutorService createScheduledThreadPool(String threadNamePrefix, Boolean daemon) {
        ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) POOLS.computeIfAbsent(threadNamePrefix, k -> {
            ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
            return Executors.newSingleThreadScheduledExecutor(threadFactory);
        });
        return scheduledExecutorService;
    }

    /**
     * Create a custom thread factory with thread name prefix and daemon thread
     *
     * @param threadNamePrefix thread name prefix
     * @param daemon           is daemon thread
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                // com.google.common.util.concurrent.ThreadFactoryBuilder
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * shutdown all thread pool
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