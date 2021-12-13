package me.hypocrite30.rpc.common.utils;

import org.junit.Test;

import java.util.Properties;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/13 22:46
 */
public class PropertiesUtilsTest {
    @Test
    public void testGetProperties() {
        Properties properties = PropertiesUtils.getProperties("rpc.properties");
        String s = (String) properties.get("crpc.etcd.endpoints");
        System.out.println(s);
    }
}