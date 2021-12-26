package me.hypocrite30.rpc.core.registry.etcd.util;

import com.google.gson.JsonSyntaxException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.utils.code.GsonSerializer;
import me.hypocrite30.rpc.core.registry.RpcRegisteredServiceInfo;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Jetcd - a java client for etcd
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/13 10:33
 */
@Slf4j
public class EtcdUtils {

    public static final String DEFAULT_ENDPOINTS = "http://192.168.248.128:2379,http://192.168.248.128:2380,http://192.168.248.128:2381";
    public static final String ETCD_REGISTRY_ROOT = "/etcd-registry";
    public static final long TIME_OUT = 5000L;
    public static final int DEFAULT_LIST_SIZE = 16;
    /**
     * @key: servicePath e.g. /etcd-registry/me.hypocrite30.rpc.core.EchoServiceGroup1Version1
     * @value: rpc registered service information
     */
    public static final Map<String, RpcRegisteredServiceInfo> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    public static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();
    private static Client etcdClient;
    private static Object lock = new Object();

    private EtcdUtils() {
    }

    /**
     * get etcd java client
     *
     * @return Etcd Client
     */
    public static KV getEtcdClient() {
        if (etcdClient == null) {
            synchronized (lock) {
                // double checked
                if (etcdClient == null) {
                    // get etcd endpoints from resources if config is existing
                    // Properties properties = PropertiesUtils.getProperties(RpcConfigEnum.RPC_CONFIG_PATH.getValue());
                    // String etcdAddress = properties != null && properties.getProperty(RpcConfigEnum.ETCD_ENDPOINTS.getValue()) != null ? properties.getProperty(RpcConfigEnum.ETCD_ENDPOINTS.getValue()) : DEFAULT_ENDPOINTS;
                    String etcdAddress = "http://192.168.248.128:2379,http://192.168.248.128:2380,http://192.168.248.128:2381";
                    etcdClient = Client.builder().endpoints(etcdAddress.split(",")).build();
                }
            }
        }
        return etcdClient.getKVClient();
    }

    public static void addServiceAddressToEtcd(KV etcdClient, String servicePath, InetSocketAddress inetSocketAddress) {
        // firstly check cache
        if (checkIfHasBeenCached(servicePath, inetSocketAddress)) {
            log.info("The node has been exsisted. Node: [{}]", servicePath);
            return;
        }
        try {
            // query to get registered service address
            RpcRegisteredServiceInfo rpcRegisteredServiceInfo = getAddressByServicePath(servicePath, etcdClient);
            if (rpcRegisteredServiceInfo == null) {
                rpcRegisteredServiceInfo = new RpcRegisteredServiceInfo();
                rpcRegisteredServiceInfo.setIP(new ArrayList<>(DEFAULT_LIST_SIZE));
            }
            if (checkIfHasBeenRegistered(rpcRegisteredServiceInfo, inetSocketAddress)) {
                log.info("The node has been exsisted. Node: [{}]", servicePath);
            } else {
                // put service address if it has not been registered
                if (putAddressByServicePath(servicePath, inetSocketAddress, etcdClient, rpcRegisteredServiceInfo)) {
                    log.info("The node has been created successfully. Node: [{}]", servicePath);
                    REGISTERED_SERVICE.add(servicePath);
                } else {
                    log.error("Fail to create node. Node: [{}]", servicePath);
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | RpcException e) {
            log.error("Registry occur some problems", e);
        }
    }

    public static boolean putAddressByServicePath(String servicePath, InetSocketAddress inetSocketAddress, KV etcdClient, RpcRegisteredServiceInfo rpcRegisteredServiceInfo) throws ExecutionException, InterruptedException, TimeoutException {
        rpcRegisteredServiceInfo.getIP().add(inetSocketAddress.toString());
        // Java Bean to Json by Gson
        String jsonServerInfo = GsonSerializer.JavaBean2Json(rpcRegisteredServiceInfo);
        CompletableFuture<PutResponse> completableFuture = etcdClient.put(bytesOf(servicePath), bytesOf(jsonServerInfo));
        PutResponse response = completableFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
        return response != null;
    }

    public static RpcRegisteredServiceInfo getAddressByServicePath(String servicePath, KV etcdClient) throws ExecutionException, InterruptedException, TimeoutException, RpcException {
        if (SERVICE_ADDRESS_MAP.containsKey(servicePath)) {
            return SERVICE_ADDRESS_MAP.get(servicePath);
        }
        CompletableFuture<GetResponse> completableFuture = etcdClient.get(bytesOf(servicePath));
        GetResponse response = completableFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
        List<KeyValue> kvs = response.getKvs();
        if (CollectionUtils.isEmpty(kvs)) {
            return null;
        }
        String jsonServerInfo = new String(kvs.get(0).getValue().getBytes());
        try {
            GsonSerializer.isJsonFormat(jsonServerInfo);
        } catch (JsonSyntaxException e) {
            // etcd has registered service but not json format, can not return null, it will initialize RpcRegisteredServiceInfo and overwrite registered service
            throw new RpcException(RpcErrorEnum.JSON_FORMAT_ERROR);
        }
        // Json to Java bean by Gson
        RpcRegisteredServiceInfo rpcRegisteredServiceInfo = GsonSerializer.Json2JavaBean(jsonServerInfo, RpcRegisteredServiceInfo.class);
        // update query result with storing the map
        SERVICE_ADDRESS_MAP.put(servicePath, rpcRegisteredServiceInfo);
        return rpcRegisteredServiceInfo;
    }

    private static boolean checkIfHasBeenCached(String servicePath, InetSocketAddress inetSocketAddress) {
        if (SERVICE_ADDRESS_MAP.containsKey(servicePath)) {
            return SERVICE_ADDRESS_MAP.get(servicePath).getIP().contains(inetSocketAddress.toString());
        }
        return false;
    }

    private static boolean checkIfHasBeenRegistered(RpcRegisteredServiceInfo rpcRegisteredServiceInfo, InetSocketAddress inetSocketAddress) {
        return rpcRegisteredServiceInfo.getIP().contains(inetSocketAddress.toString());
    }

    public static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, UTF_8);
    }
}