package me.hypocrite30.rpc.common.utils.code;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Java bean serialize into Json or opposite operation based on Gson
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/21 21:54
 */
public class GsonSerializer {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    private GsonSerializer() {
    }

    public static String JavaBean2Json(Object javaBean) {
        return gson.toJson(javaBean);
    }

    public static <T> T Json2JavaBean(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
