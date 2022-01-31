package me.hypocrite30.rpc.core.registry.etcd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * rpc client pull etcd registered services list regularly
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/29 13:59
 */
@Slf4j
public class ScheduledPullServicesList {

    public static void main(String[] args) {
        // AtomicInteger i = new AtomicInteger(1);
        // ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
        //         createThreadFactory("schedule-pool", false));
        // service.scheduleAtFixedRate(() -> {
        //     log.info("current time {}: test{}", System.currentTimeMillis() / 1000, i.getAndIncrement());
        // }, 0, 1, TimeUnit.SECONDS);

        EtcdScheduledUpdater etcdScheduledUpdater = new EtcdScheduledUpdater();
        etcdScheduledUpdater.start(0, 5);

    }

    /**
     * Using Guava to create thread factory with thread name prefix and daemon thread
     *
     * @param threadNamePrefix thread pool name prefix
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
}
