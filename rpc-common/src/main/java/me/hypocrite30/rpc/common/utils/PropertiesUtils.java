package me.hypocrite30.rpc.common.utils;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.enums.RpcErrorEnum;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * get value from properties file
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/13 22:24
 */
@Slf4j
public class PropertiesUtils {
    private PropertiesUtils() {
    }

    public static Properties getProperties(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = "";
        if (url != null) {
            path = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error(RpcErrorEnum.FAIL_TO_GET_PROPERTIES.getErrorMsg() + " FileName [{}]", fileName);
        }
        return properties;
    }
}
