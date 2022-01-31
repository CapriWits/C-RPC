package me.hypocrite30.rpc.common.utils.code;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Java bean serialize into Json or opposite operation based on Gson
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/21 21:54
 */
@Slf4j
public class GsonSerializer {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private GsonSerializer() {
    }

    public static String JavaBean2Json(Object javaBean) {
        String json = gson.toJson(javaBean);
        if (json != null) {
            log.info("Java bean: [{}] has been serialized to Json: {}", javaBean, json);
        }
        return json;
    }

    public static <T> T Json2JavaBean(String json, Class<T> clazz) {
        T javaBean = gson.fromJson(json, clazz);
        if (json != null) {
            log.info("Json: [{}] has been deserialized to Java bean: {}", json, javaBean);
        }
        return javaBean;
    }

    public static boolean isJsonFormat(String json) {
        JsonElement jsonElement = new JsonParser().parse(json);
        return jsonElement != null && jsonElement.isJsonObject();
    }
}