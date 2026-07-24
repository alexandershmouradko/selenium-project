package uk.ndc.csa.utilities.selenium.driver.factory;

import java.util.Map;
import java.util.Set;
import org.openqa.selenium.MutableCapabilities;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.ThreadContext;

/** Builds W3C-compatible capabilities from the browser matrix and runtime properties. */
public final class Capabilities {
    private static final Set<String> STANDARD_KEYS = Set.of(
            "acceptInsecureCerts",
            "pageLoadStrategy",
            "proxy",
            "timeouts",
            "unhandledPromptBehavior",
            "strictFileInteractability",
            "webSocketUrl",
            "setWindowRect");

    private final MutableCapabilities capabilities = new MutableCapabilities();

    public Capabilities() {
        ThreadContext context = ThreadContext.getInstance();
        setIfPresent("browserName", normalizeBrowserName(context.getBrowserName()));
        setIfPresent("browserVersion", context.getBrowserVersion());
        String server = context.getBrowserCombo().getOrDefault("seleniumServer", "local");
        if (!"browserstack".equalsIgnoreCase(server)
                || context.getBrowserCombo().containsKey("platform")
                || context.getBrowserCombo().containsKey("platformName")) {
            setIfPresent("platformName", context.getPlatform());
        }

        for (Map.Entry<String, String> entry : context.getBrowserCombo().entrySet()) {
            String key = entry.getKey();
            if (isFrameworkMetadata(key) || isBrowserAlias(key)) {
                continue;
            }
            addValidatedCapability(key, typedValue(entry.getValue()));
        }

        FrameworkProperties properties = context.getSeleniumProps();
        String browser = normalizeBrowserName(context.getBrowserName());
        String browserKey = browser == null ? "browser" : browser.replaceAll("\\s", "");
        for (String variable : properties.getStringArray("desiredCapabilities." + browserKey)) {
            String[] pair = variable.split("==", 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Invalid desired capability, expected key==value: " + variable);
            }
            addValidatedCapability(pair[0].trim(), typedValue(pair[1].trim()));
        }
    }

    public MutableCapabilities getCap() {
        return capabilities;
    }

    private void addValidatedCapability(String key, Object value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        if (STANDARD_KEYS.contains(key) || key.contains(":")) {
            capabilities.setCapability(key, value);
            return;
        }
        throw new IllegalArgumentException(
                "Custom W3C capability must be vendor-namespaced (for example vendor:options): " + key);
    }

    private void setIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            capabilities.setCapability(key, value);
        }
    }

    private static boolean isFrameworkMetadata(String key) {
        return Set.of("seleniumServer", "deviceName", "name", "build").contains(key);
    }

    private static boolean isBrowserAlias(String key) {
        return Set.of("browser", "browserName", "version", "browserVersion", "browser_version",
                "platform", "platformName", "os", "os_version").contains(key);
    }

    private static String normalizeBrowserName(String value) {
        if (value == null || value.isBlank()) return null;
        String browser = value.trim().toLowerCase();
        if (browser.startsWith("chrome")) return "chrome";
        if (browser.startsWith("firefox")) return "firefox";
        if (browser.equals("microsoftedge") || browser.startsWith("edge")) return "MicrosoftEdge";
        if (browser.startsWith("safari")) return "safari";
        return value.trim();
    }

    private static Object typedValue(String value) {
        if (value == null) return null;
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        if (value.matches("-?\\d+")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                // Keep very large values as strings.
            }
        }
        return value;
    }
}
