package uk.ndc.csa.utilities.common;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

/** Property and environment lookup utilities. */
public final class Property {
    public static final String PATHPROP = "/src/test/resources/config/paths.properties";

    private Property() {
    }

    public static FrameworkProperties getProperties(String propertiesPath) {
        if (propertiesPath == null || propertiesPath.isBlank()) {
            return FrameworkProperties.empty();
        }
        try {
            return FrameworkProperties.load(Path.of(normalize(propertiesPath)));
        } catch (Exception ignored) {
            return FrameworkProperties.empty();
        }
    }

    public static String getProperty(String key) {
        return getProperties(System.getProperty("user.dir") + PATHPROP).getString(key);
    }

    public static String getProperty(String propertiesPath, String key) {
        return getProperties(propertiesPath).getString(key);
    }

    public static String[] getPropertyArray(String propertiesPath, String key) {
        return getProperties(propertiesPath).getStringArray(key);
    }

    /** System property wins, followed by the exact environment variable and then ENV_STYLE_NAME. */
    public static String getVariable(String name) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }
        value = System.getenv(name);
        if (value != null) {
            return value;
        }
        return System.getenv(name.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT));
    }

    public static String requireVariable(String name) {
        String value = getVariable(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required variable is missing: " + name);
        }
        return value;
    }

    private static String normalize(String path) {
        return path.replace("/", File.separator).replace("\\", File.separator);
    }
}
