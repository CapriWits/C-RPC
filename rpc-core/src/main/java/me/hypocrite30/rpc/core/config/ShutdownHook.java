package me.hypocrite30.rpc.core.config;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.utils.concurrent.threadpool.ThreadPoolUtils;
import me.hypocrite30.rpc.common.utils.net.NetUtils;
import me.hypocrite30.rpc.core.registry.etcd.util.EtcdUtils;

import java.net.InetSocketAddress;

import static me.hypocrite30.rpc.core.remote.transport.netty.server.NettyRpcServer.PORT;

/**
 * unregister service manually when server is closed normally
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/7 22:40
 */
@Slf4j
public class ShutdownHook {
    public static final ShutdownHook SHUTDOWN_HOOK = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return SHUTDOWN_HOOK;
    }

    private ShutdownHook() {
    }

    public void unregister() {
        log.info("add shutdown hook for unregistering");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            InetSocketAddress inetAddress = new InetSocketAddress(NetUtils.getLocalHostExactAddress(), PORT);
            EtcdUtils.unregister(inetAddress, EtcdUtils.getEtcdClient());
            ThreadPoolUtils.shutDownAllThreadPool();
        }));
    }
}
