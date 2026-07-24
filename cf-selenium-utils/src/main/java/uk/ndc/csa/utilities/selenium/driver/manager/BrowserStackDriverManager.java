package uk.ndc.csa.utilities.selenium.driver.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.MutableCapabilities;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;
import uk.ndc.csa.utilities.selenium.driver.factory.RemoteDriverHelper;

/** BrowserStack driver using W3C {@code bstack:options}; credentials are not embedded in the URL. */
public class BrowserStackDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        String endpoint = defaultIfBlank(Property.getVariable("cukes.browserstackUrl"),
                "https://hub-cloud.browserstack.com/wd/hub");
        MutableCapabilities capabilities = new Capabilities().getCap();
        Map<String, Object> options = optionMap(capabilities, "bstack:options");
        options.putIfAbsent("userName", Property.requireVariable("cukes.browserstackUserName"));
        options.putIfAbsent("accessKey", Property.requireVariable("cukes.browserstackAccessKey"));
        Map<String, String> browserCombo = ThreadContext.getInstance().getBrowserCombo();
        putIfNotBlank(options, "os", browserCombo.get("os"));
        putIfNotBlank(options, "osVersion", browserCombo.get("os_version"));
        putIfNotBlank(options, "deviceName", browserCombo.get("deviceName"));
        putIfNotBlank(options, "resolution", browserCombo.get("resolution"));
        options.putIfAbsent("sessionName", defaultIfBlank(ThreadContext.getInstance().getScenario(), "Cucumber scenario"));
        options.putIfAbsent("buildName", buildName());
        capabilities.setCapability("bstack:options", options);
        try {
            driver = RemoteDriverHelper.getRemoteDriver(new URL(endpoint), capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid BrowserStack URL", e);
        }
    }

    private static Map<String, Object> optionMap(MutableCapabilities capabilities, String key) {
        Object existing = capabilities.getCapability(key);
        Map<String, Object> result = new HashMap<>();
        if (existing instanceof Map<?, ?> map) {
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
        }
        return result;
    }

    private static void putIfNotBlank(Map<String, Object> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.putIfAbsent(key, value);
        }
    }

    private static String buildName() {
        return defaultIfBlank(Property.getVariable("JOB_NAME"), "Adhoc") + "_"
                + defaultIfBlank(Property.getVariable("BUILD_NUMBER"), "Adhoc");
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
