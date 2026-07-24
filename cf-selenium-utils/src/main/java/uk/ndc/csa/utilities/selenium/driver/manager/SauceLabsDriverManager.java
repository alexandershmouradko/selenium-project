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

/** Sauce Labs driver using W3C {@code sauce:options}; credentials are not embedded in the URL. */
public class SauceLabsDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        String endpoint = Property.getVariable("cukes.sauceUrl");
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = normalizeEndpoint(Property.requireVariable("cukes.sauceEndPoint"));
        }
        MutableCapabilities capabilities = new Capabilities().getCap();
        Map<String, Object> options = optionMap(capabilities, "sauce:options");
        options.putIfAbsent("username", Property.requireVariable("cukes.sauceUserName"));
        options.putIfAbsent("accessKey", Property.requireVariable("cukes.sauceAccessKey"));
        options.putIfAbsent("name", defaultIfBlank(ThreadContext.getInstance().getScenario(), "Cucumber scenario"));
        options.putIfAbsent("build", buildName());
        capabilities.setCapability("sauce:options", options);
        try {
            driver = RemoteDriverHelper.getRemoteDriver(new URL(endpoint), capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Sauce Labs URL", e);
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

    private static String normalizeEndpoint(String endpoint) {
        return endpoint.startsWith("http://") || endpoint.startsWith("https://") ? endpoint : "https://" + endpoint;
    }

    private static String buildName() {
        return defaultIfBlank(Property.getVariable("JOB_NAME"), "Adhoc") + "_"
                + defaultIfBlank(Property.getVariable("BUILD_NUMBER"), "Adhoc");
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
