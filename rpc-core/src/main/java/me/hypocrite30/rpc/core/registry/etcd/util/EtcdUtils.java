package me.hypocrite30.rpc.core.registry.etcd.util;

import com.google.gson.JsonSyntaxException;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.shaded.io.grpc.stub.CallStreamObserver;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcConfigEnum;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.common.utils.PropertiesUtils;
import me.hypocrite30.rpc.common.utils.code.GsonSerializer;
import me.hypocrite30.rpc.core.registry.RpcRegisteredServiceInfo;
import me.hypocrite30.rpc.core.remote.transport.netty.codec.RpcCodecConstants;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

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
     * @key: rpc server host
     * @value: rpc registered service information
     */
    public static final Map<String, RpcRegisteredServiceInfo> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static Client etcdClient;
    private static Object lock = new Object();
    private static long leaseId = 0L;

    private EtcdUtils() {
    }

    /**
     * get etcd java client
     *
     * @return Etcd Client
     */
    public static Client getEtcdClient() {
        if (etcdClient == null) {
            synchronized (lock) {
                // double checked
                if (etcdClient == null) {
                    // get etcd endpoints from resources if config is existing
                    Properties properties = PropertiesUtils.getProperties(RpcConfigEnum.RPC_CONFIG_PATH.getValue());
                    String etcdAddress = properties != null && properties.getProperty(RpcConfigEnum.ETCD_ENDPOINTS.getValue()) != null ?
                            properties.getProperty(RpcConfigEnum.ETCD_ENDPOINTS.getValue()) :
                            DEFAULT_ENDPOINTS;
                    etcdClient = Client.builder().endpoints(etcdAddress.split(",")).build();
                }
            }
        }
        return etcdClient;
    }

    /**
     * registry method. Etcd store structure: K-V [server ip:host - service list]
     *
     * @param etcdClient        etcd java client
     * @param servicePath       e.g. /etcd-registry/me.hypocrite30.rpc.core.EchoService#Group1#Version1
     * @param inetSocketAddress e.g. /127.0.0.1:9996
     */
    public static void addServiceAddressToEtcd(Client etcdClient, String servicePath, InetSocketAddress inetSocketAddress) {
        // firstly check cache
        if (checkIfHasBeenCached(servicePath, inetSocketAddress)) {
            log.info("The node has been exsisted. Node: [{}]", servicePath);
            return;
        }
        try {
            // query to get registered service
            RpcRegisteredServiceInfo rpcRegisteredServiceInfo = getServicePathByAddress(inetSocketAddress, etcdClient);
            if (rpcRegisteredServiceInfo == null) {
                rpcRegisteredServiceInfo = new RpcRegisteredServiceInfo();
                rpcRegisteredServiceInfo.setServicePath(new ArrayList<>(DEFAULT_LIST_SIZE));
            }
            if (checkIfHasBeenRegistered(rpcRegisteredServiceInfo, servicePath)) {
                log.info("The node has been exsisted. Node: [{}]", servicePath);
            } else {
                // put service if it has not been registered
                // add service to registered service IP list firstly
                rpcRegisteredServiceInfo.getServicePath().add(servicePath);
                putWithLease(inetSocketAddress.toString(), rpcRegisteredServiceInfo, etcdClient);
                log.info("The node has been created successfully. Node: [{} - {}]", inetSocketAddress, servicePath);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | RpcException e) {
            throw new RpcException("Registry occur some problems", e);
        }
    }

    /**
     * Get registered service information by service path
     *
     * @param inetSocketAddress e.g. /etcd-registry/me.hypocrite30.rpc.core.EchoService#Group1#Version1
     * @param etcdClient        etcd java client
     * @return registered service information object
     */
    public static RpcRegisteredServiceInfo getServicePathByAddress(InetSocketAddress inetSocketAddress, Client etcdClient) throws ExecutionException, InterruptedException, TimeoutException, RpcException {
        if (SERVICE_ADDRESS_MAP.containsKey(inetSocketAddress.toString())) {
            return SERVICE_ADDRESS_MAP.get(inetSocketAddress.toString());
        }
        CompletableFuture<GetResponse> completableFuture = etcdClient.getKVClient().get(bytesOf(inetSocketAddress.toString()));
        GetResponse response = completableFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
        List<KeyValue> kvs = response.getKvs();
        if (CollectionUtils.isEmpty(kvs)) {
            return null;
        }
        String jsonServiceInfo = new String(kvs.get(0).getValue().getBytes());
        try {
            GsonSerializer.isJsonFormat(jsonServiceInfo);
        } catch (JsonSyntaxException e) {
            // etcd has registered service but not json format, can not return null, it will initialize RpcRegisteredServiceInfo and overwrite registered service later
            throw new RpcException(RpcErrorEnum.JSON_FORMAT_ERROR);
        }
        // Json string transform to Java bean by Gson
        RpcRegisteredServiceInfo rpcRegisteredServiceInfo = GsonSerializer.Json2JavaBean(jsonServiceInfo, RpcRegisteredServiceInfo.class);
        // update query result with storing the map
        SERVICE_ADDRESS_MAP.put(inetSocketAddress.toString(), rpcRegisteredServiceInfo);
        return rpcRegisteredServiceInfo;
    }

    /**
     * put K-V to etcd with lease and keep lease alive forever
     *
     * @param host                     rpc service ip:port
     * @param rpcRegisteredServiceInfo service information
     */
    public static void putWithLease(String host, RpcRegisteredServiceInfo rpcRegisteredServiceInfo, Client etcdClient) {
        String jsonServiceInfo = GsonSerializer.JavaBean2Json(rpcRegisteredServiceInfo);
        Lease leaseClient = etcdClient.getLeaseClient();
        if (leaseId != 0L) {
            log.info("The old leaseId: [{}] has deleted", leaseId);
            leaseClient.revoke(leaseId);
        }
        leaseClient.grant(30).thenAccept(result -> {
            leaseId = result.getID();
            log.info("key: [{}] has offered a lease successfully, lease ID: [{}]", host, Long.toHexString(leaseId));
            PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
            etcdClient.getKVClient()
                    .put(bytesOf(host), bytesOf(jsonServiceInfo), putOption)
                    // keep the given lease alive forever
                    .thenAccept(putResponse -> leaseClient.keepAlive(leaseId, new CallStreamObserver<LeaseKeepAliveResponse>() {
                        @Override
                        public boolean isReady() {
                            return false;
                        }

                        @Override
                        public void setOnReadyHandler(Runnable runnable) {}

                        @Override
                        public void disableAutoInboundFlowControl() {}

                        @Override
                        public void request(int i) {}

                        @Override
                        public void setMessageCompression(boolean b) {}

                        @Override
                        public void onNext(LeaseKeepAliveResponse keepAliveResponse) {
                            log.info("[{}] lease keep alive successfully, TTL: [{}]", Long.toHexString(leaseId), keepAliveResponse.getTTL());
                        }

                        @Override
                        public void onError(Throwable throwable) {}

                        @Override
                        public void onCompleted() {
                            log.info("onCompleted");
                        }
                    }));
        });
    }

    /**
     * get all etcd key and value with prefix
     *
     * @param prefix     all key common prefix
     * @param etcdClient etcd java client
     * @return map for host - services list
     */
    public static List<KeyValue> getAllEtcdKVs(String prefix, Client etcdClient) throws ExecutionException, InterruptedException, TimeoutException {
        KV client = etcdClient.getKVClient();
        GetOption getOption = GetOption.newBuilder().withPrefix(bytesOf(prefix)).build();
        GetResponse response = client.get(bytesOf(prefix), getOption).get(TIME_OUT, TimeUnit.MILLISECONDS);
        return response.getKvs();
    }

    /**
     * Check if service address has been cached
     *
     * @param servicePath       e.g. /etcd-registry/me.hypocrite30.rpc.core.EchoService#Group1#Version1
     * @param inetSocketAddress service address ready to be registered
     * @return true if service address has been cached
     */
    private static boolean checkIfHasBeenCached(String servicePath, InetSocketAddress inetSocketAddress) {
        if (SERVICE_ADDRESS_MAP.containsKey(inetSocketAddress.toString())) {
            return SERVICE_ADDRESS_MAP.get(inetSocketAddress.toString()).getServicePath().contains(servicePath);
        }
        return false;
    }

    /**
     * check if service address has been registered
     *
     * @param rpcRegisteredServiceInfo registered service information
     * @param servicePath              service fully qualified name with group and version
     * @return true if service address has been registered
     */
    private static boolean checkIfHasBeenRegistered(RpcRegisteredServiceInfo rpcRegisteredServiceInfo, String servicePath) {
        return rpcRegisteredServiceInfo.getServicePath().contains(servicePath);
    }

    /**
     * Transfer [java.lang.String] to [io.etcd.jetcd.ByteSequence]
     *
     * @param val java.lang.String
     * @return io.etcd.jetcd.ByteSequence
     */
    public static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, RpcCodecConstants.DEFAULT_CHARSET);
    }

    /**
     * Service Path: /etcd-registry/me.hypocrite30.rpc.api.EchoService#Group1#Version1
     *
     * @param rpcServiceName e.g. me.hypocrite30.rpc.api.EchoService#g1#v1
     * @return Service Path
     */
    public static String getServicePath(String rpcServiceName) {
        return ETCD_REGISTRY_ROOT + "/" + rpcServiceName;
    }

    /**
     * Rpc Service Name: me.hypocrite30.rpc.api.EchoService#Group1#Version1
     *
     * @param servicePath e.g. /etcd-registry/me.hypocrite30.rpc.api.EchoService#Group1#Version1
     * @return Rpc Service Name
     */
    public static String getRpcServiceName(String servicePath) {
        return servicePath.split("/")[2];
    }
}