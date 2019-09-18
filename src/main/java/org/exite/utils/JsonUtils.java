package org.exite.utils;

import com.google.gson.Gson;

/**
 * Created by levitsky on 11.02.19
 */
public class JsonUtils {

    private static final Gson gson = new Gson();

    public static Object fromJson(String json, Class<? extends Object> c) {
        return gson.fromJson(json, c);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }
}
