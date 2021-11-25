package me.hypocrite30.test;

import io.etcd.jetcd.Response.Header;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;

/**
 * @Description: Etcd Service Interface
 * @Author: Hypocrite30
 * @Date: 2021/11/24 22:15
 */
public interface EtcdService {

    /**
     * 写入
     */
    Header put(String key, String value) throws Exception;

    /**
     * 读取
     */
    String getSingle(String key) throws Exception;

    /**
     * 带额外条件的查询操作，例如前缀、结果排序等
     */
    GetResponse getRange(String key, GetOption getOption) throws Exception;

    /**
     * 单个删除
     */
    long deleteSingle(String key) throws Exception;

    /**
     * 范围删除
     */
    long deleteRange(String key, DeleteOption deleteOption) throws Exception;

    /**
     * 关闭，释放资源
     */
    void close();
}

