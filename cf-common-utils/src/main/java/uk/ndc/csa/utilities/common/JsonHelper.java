package uk.ndc.csa.utilities.common;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/** JSON file and merge utilities. */
public final class JsonHelper {
    private JsonHelper() {
    }

    public static JSONObject getJSONData(String filepath, String... key) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filepath), StandardCharsets.UTF_8)) {
            JSONObject root = new JSONObject(new JSONTokener(reader));
            return key.length > 0 ? root.getJSONObject(key[0]) : root;
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON file cannot be read: " + filepath, e);
        }
    }

    public static JSONArray getJSONArray(String filepath, String... key) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filepath), StandardCharsets.UTF_8)) {
            JSONTokener token = new JSONTokener(reader);
            return key.length > 0
                    ? new JSONObject(token).getJSONArray(key[0])
                    : new JSONArray(token);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON array file cannot be read: " + filepath, e);
        }
    }

    public static <T> T getData(String path, String dataGroup, Class<T> type) throws IOException {
        Path file = Path.of(path, dataGroup + ".json");
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, type);
        }
    }

    public static Map<String, String> getJSONToMap(JSONObject json) {
        Map<String, String> map = new HashMap<>();
        String[] keys = JSONObject.getNames(json);
        if (keys != null) {
            for (String key : keys) {
                map.put(key, String.valueOf(json.get(key)));
            }
        }
        return map;
    }

    /** Recursively overlays source values onto target and returns target. */
    public static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        String[] keys = JSONObject.getNames(source);
        if (keys == null) {
            return target;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (!target.has(key)) {
                target.put(key, value);
            } else if (value instanceof JSONObject sourceObject
                    && target.get(key) instanceof JSONObject targetObject) {
                jsonMerge(sourceObject, targetObject);
            } else if (value instanceof JSONArray sourceArray
                    && target.get(key) instanceof JSONArray targetArray) {
                mergeArrays(sourceArray, targetArray);
            } else {
                target.put(key, value);
            }
        }
        return target;
    }

    private static void mergeArrays(JSONArray source, JSONArray target) {
        for (int index = 0; index < source.length(); index++) {
            Object sourceValue = source.get(index);
            if (index < target.length()
                    && sourceValue instanceof JSONObject sourceObject
                    && target.get(index) instanceof JSONObject targetObject) {
                jsonMerge(sourceObject, targetObject);
            } else if (index < target.length()) {
                target.put(index, sourceValue);
            } else {
                target.put(sourceValue);
            }
        }
    }
}
