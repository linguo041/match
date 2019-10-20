package com.roy.football.match.util;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The Json converter used google gson tool. Please try not to customize too
 * many converter and use the already defined one.
 *
 * @author linguo
 *
 */
public final class GsonConverter {

    // don't want to register in a web container
    // since it may be used not only in a web app.

    private static final String NORMAL_GSON = "nomal";
    private static ConcurrentHashMap<String, GsonConverter>
        customizedGsons = new ConcurrentHashMap<String, GsonConverter>();

    private Gson gson;

    private GsonConverter (Gson gson) {
        this.gson = gson;
    }

    public static GsonConverter useNormalGson () {
        customizedGsons.putIfAbsent(NORMAL_GSON,
        		new GsonConverter(new GsonBuilder().create()));
        return customizedGsons.get(NORMAL_GSON);
    }

    public static GsonConverter useCustomizedGson (String customizedName,
            GsonBuilder gb) {
        if (customizedName == null || customizedName.isEmpty() || gb == null) {
            return null;
        }

        return customizedGsons.putIfAbsent(customizedName,
                new GsonConverter(gb.create()));
    }

    public static GsonConverter useCustomizedGson (String customizedName) {
        return customizedGsons.get(customizedName);
    }

    public static GsonConverter removeCustomizedGson (String customizedName) {
        return customizedGsons.remove(customizedName);
    }
    
    public static <T> T convertJSonToObjectUseNormal(String json, Class<T> clazz) {
        return useNormalGson().convertJSonToObject(json, clazz);
    }

    public static <T> T convertJSonToObjectUseNormal(String json, TypeToken<T> typeToken) {
        return useNormalGson().convertJSonToObject(json, typeToken);
    }

    public static <T> String convertObjectToJsonUseNormal(T obj, Type type) {
        return useNormalGson().convertObjectToJson(obj, type);
    }

	public <T> T convertJSonToObject(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	public <T> T convertJSonToObject(String json, TypeToken<T> typeToken) {
		return gson.fromJson(json, typeToken.getType());
	}

	public <T> String convertObjectToJson(T obj, Type type) {
		return gson.toJson(obj, type);
	}
}
