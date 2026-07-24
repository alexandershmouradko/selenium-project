package uk.ndc.csa.utilities.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Small framework-owned wrapper around {@link Properties}. It keeps the useful
 * typed accessors from the legacy Commons Configuration API without exposing a
 * third-party configuration type in the public framework API.
 */
public final class FrameworkProperties {
    private final Properties values;

    private FrameworkProperties(Properties values) {
        this.values = values;
    }

    public static FrameworkProperties empty() {
        return new FrameworkProperties(new Properties());
    }

    public static FrameworkProperties load(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return new FrameworkProperties(properties);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public String getString(String key) {
        return values.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }

    public int getInt(String key) {
        String value = require(key);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Property '" + key + "' is not an integer: " + value, e);
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(require(key).trim());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public String[] getStringArray(String key) {
        String value = getString(key);
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return split(value).toArray(String[]::new);
    }

    public Iterator<String> getKeys() {
        List<String> keys = new ArrayList<>(values.stringPropertyNames());
        Collections.sort(keys);
        return keys.iterator();
    }

    public Properties asPropertiesCopy() {
        Properties copy = new Properties();
        copy.putAll(values);
        return copy;
    }

    private String require(String key) {
        String value = getString(key);
        if (value == null) {
            throw new IllegalArgumentException("Required property is missing: " + key);
        }
        return value;
    }

    private static List<String> split(String value) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (char ch : value.toCharArray()) {
            if (escaped) {
                current.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == ',') {
                String item = current.toString().trim();
                if (!item.isEmpty()) {
                    result.add(item);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        String item = current.toString().trim();
        if (!item.isEmpty()) {
            result.add(item);
        }
        return result;
    }
}
