package uk.ndc.csa.utilities.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.SoftAssert;

/** Thread-confined execution context for scenario, configuration and test data. */
public final class ThreadContext {
    private static final ThreadLocal<ThreadContext> CONTEXT = ThreadLocal.withInitial(ThreadContext::new);

    private final Logger logger = LogManager.getLogger(ThreadContext.class);
    private String feature;
    private String scenario;
    private SoftAssert softAssert = new SoftAssert();
    private final Map<String, Object> testData = new HashMap<>();
    private FrameworkProperties pathsProps = FrameworkProperties.empty();
    private FrameworkProperties environmentProps = FrameworkProperties.empty();
    private FrameworkProperties seleniumProps = FrameworkProperties.empty();
    private boolean selenium;
    private Map<String, String> browserCombo = Map.of();
    private boolean keepBrowserOpen;

    private ThreadContext() {
    }

    public static ThreadContext getInstance() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public void setThreadContext(Boolean selenium, Map<String, String> browserCombo) {
        setPathsProps();
        setEnvironmentProps();
        setSelenium(Boolean.TRUE.equals(selenium));
        if (this.selenium) {
            setSeleniumProps();
            setBrowserCombo(browserCombo);
        } else {
            setBrowserCombo(Map.of());
        }
    }

    public Boolean getSelenium() { return selenium; }
    public String getFeature() { return feature; }
    public String getScenario() { return scenario; }
    public Map<String, String> getBrowserCombo() { return browserCombo; }
    public FrameworkProperties getSeleniumProps() { return seleniumProps; }
    public FrameworkProperties getEnvironmentProps() { return environmentProps; }
    public FrameworkProperties getPathsProps() { return pathsProps; }
    public Logger getLogger() { return logger; }
    public Boolean getKeepBrowserOpen() { return keepBrowserOpen; }

    public String getBrowserName() {
        return firstNonBlank(browserCombo.get("browserName"), browserCombo.get("browser"));
    }

    public String getBrowserVersion() {
        return firstNonBlank(browserCombo.get("browserVersion"), browserCombo.get("version"), browserCombo.get("browser_version"));
    }

    public String getPlatform() {
        String platform = browserCombo.get("platform");
        if (platform != null && !platform.isBlank()) {
            return platform;
        }
        String os = browserCombo.get("os");
        String osVersion = browserCombo.get("os_version");
        return os == null ? null : osVersion == null ? os : os + "_" + osVersion;
    }

    public void setSelenium(Boolean selenium) { this.selenium = Boolean.TRUE.equals(selenium); }
    public void setFeature(String value) { this.feature = value; }
    public void setScenario(String value) { this.scenario = value; }
    public void setBrowserCombo(Map<String, String> value) {
        this.browserCombo = value == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(value));
    }
    public void setKeepBrowserOpen(Boolean value) { this.keepBrowserOpen = Boolean.TRUE.equals(value); }

    public void setSeleniumProps() {
        String relativePath = pathsProps.getString("seleniumRuntimePath", "/src/test/resources/config/selenium/");
        seleniumProps = Property.getProperties(System.getProperty("user.dir") + relativePath + "runtime.properties");
        if (seleniumProps.isEmpty()) {
            logger.warn("No Selenium runtime properties found at {}runtime.properties", relativePath);
        }
    }

    public void setEnvironmentProps() {
        String environment = Property.getVariable("cukes.env");
        if (environment == null || environment.isBlank()) {
            environment = "local";
        }
        String relativePath = pathsProps.getString("environmentsPath", "/src/test/resources/config/environments/");
        environmentProps = Property.getProperties(System.getProperty("user.dir") + relativePath + environment + ".properties");
        if (environmentProps.isEmpty()) {
            logger.warn("No environment properties found for '{}' at {}", environment, relativePath);
        }
    }

    public void setPathsProps() {
        pathsProps = Property.getProperties(System.getProperty("user.dir") + Property.PATHPROP);
        if (pathsProps.isEmpty()) {
            logger.warn("No paths properties found at {}", Property.PATHPROP);
        }
    }

    public SoftAssert sa() { return softAssert; }
    public void resetSoftAssert() { softAssert = new SoftAssert(); }
    public Map<String, Object> testdata() { return testData; }
    public Object testdataGet(String key) { return testData.get(key); }
    public <T> T testdataToClass(String key, Class<T> type) { return type.cast(testData.get(key)); }
    public void testdataPut(String key, Object data) { testData.put(key, data); }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
