package me.hypocrite30.rpc.core.registry.etcd;

import com.google.gson.JsonSyntaxException;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.common.utils.code.GsonSerializer;
import me.hypocrite30.rpc.common.utils.concurrent.threadpool.ThreadPoolUtils;
import me.hypocrite30.rpc.core.registry.RpcRegisteredServiceInfo;
import me.hypocrite30.rpc.core.registry.etcd.util.EtcdUtils;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Rpc client scheduled pull registered services list from etcd registration center
 *
 * @Author: Hypocrite30
 * @Date: 2022/1/30 0:25
 */
@Slf4j
public class EtcdScheduledUpdater implements Runnable {

    /**
     * @key: rpc service name: e.g. me.hypocrite30.rpc.core.EchoService#Group1#Version1
     * @value: registered services host list
     */
    protected static Map<String, List<String>> registeredServices;
    private static ScheduledExecutorService scheduledExecutorService;

    public EtcdScheduledUpdater() {
        scheduledExecutorService = ThreadPoolUtils.createScheduledThreadPool("rpc-services-list-updater", false);
        registeredServices = SingletonFactory.getInstance(ConcurrentHashMap.class);
    }

    /**
     * pull and update registered services list at regular intervals
     *
     * @param initialDelay scheduled task start time
     * @param period       scheduled task intervals
     */
    public void start(long initialDelay, long period) {
        scheduledExecutorService.scheduleAtFixedRate(this, initialDelay, period, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Client etcdClient = EtcdUtils.getEtcdClient();
        try {
            List<KeyValue> KVs = EtcdUtils.getAllEtcdKVs("/", etcdClient);
            updateCache(KVs);
            log.info("scheduled update task has finished");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RpcException("scheduled update task occur some problems", e);
        }
    }

    private static void updateCache(List<KeyValue> KVs) {
        if (CollectionUtils.isEmpty(KVs)) {
            throw new RpcException(RpcErrorEnum.NOT_FOUND_SERVICE);
        }
        for (KeyValue kv : KVs) {
            String host = kv.getKey().toString(StandardCharsets.UTF_8);
            String jsonServicesList = new String(kv.getValue().getBytes());
            log.info("host: [{}], servicesList: [{}]", host, jsonServicesList);
            try {
                GsonSerializer.isJsonFormat(jsonServicesList);
            } catch (JsonSyntaxException e) {
                throw new RpcException(RpcErrorEnum.JSON_FORMAT_ERROR);
            }
            RpcRegisteredServiceInfo rpcRegisteredServiceInfo = GsonSerializer.Json2JavaBean(jsonServicesList, RpcRegisteredServiceInfo.class);
            for (String servicePath : rpcRegisteredServiceInfo.getServicePath()) {
                String rpcServiceName = EtcdUtils.getRpcServiceName(servicePath);
                List<String> hostsList = registeredServices.get(rpcServiceName);
                if (hostsList == null) {
                    hostsList = new ArrayList<>();
                }
                if (!hostsList.contains(host)) {
                    hostsList.add(host);
                }
                registeredServices.put(rpcServiceName, hostsList);
            }
        }
        log.info("current services list cache: [{}]", registeredServices);
    }
}