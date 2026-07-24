package uk.ndc.csa.utilities.selenium.driver.factory;

import java.util.Locale;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.manager.BrowserStackDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.ChromeDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.EdgeDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.FirefoxDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.GridDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.IEDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.PerfectoDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.SafariDriverManager;
import uk.ndc.csa.utilities.selenium.driver.manager.SauceLabsDriverManager;

/** Thread-safe WebDriver factory for local and remote execution providers. */
public final class DriverFactory {
    public enum ServerType { local, grid, saucelabs, browserstack, perfecto }
    public enum BrowserType { chrome, firefox, edge, safari }

    private static final DriverFactory INSTANCE = new DriverFactory();
    private final ThreadLocal<DriverManager> driverManager = ThreadLocal.withInitial(this::createManager);

    private DriverFactory() {
    }

    public static DriverFactory getInstance() { return INSTANCE; }
    public DriverManager driverManager() { return driverManager.get(); }
    public WebDriver getDriver() { return driverManager.get().getDriver(); }
    public WebDriverWait getWait() { return driverManager.get().getWait(); }

    public void quit() {
        DriverManager manager = driverManager.get();
        try {
            manager.quitDriver();
        } finally {
            driverManager.remove();
        }
    }

    /** Retained for source compatibility. */
    public DriverManager setDM() { return driverManager.get(); }

    private DriverManager createManager() {
        String serverValue = ThreadContext.getInstance().getBrowserCombo().getOrDefault("seleniumServer", "local");
        ServerType serverType;
        try {
            serverType = ServerType.valueOf(serverValue.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unsupported Selenium server type: " + serverValue, e);
        }

        return switch (serverType) {
            case grid -> new GridDriverManager();
            case saucelabs -> new SauceLabsDriverManager();
            case browserstack -> new BrowserStackDriverManager();
            case perfecto -> new PerfectoDriverManager();
            case local -> localManager(ThreadContext.getInstance().getBrowserName());
        };
    }

    private DriverManager localManager(String browserName) {
        String browser = browserName == null ? "chrome" : browserName.toLowerCase(Locale.ROOT);
        if (browser.startsWith("chrome")) return new ChromeDriverManager();
        if (browser.startsWith("firefox")) return new FirefoxDriverManager();
        if (browser.equals("edge") || browser.equals("microsoftedge")) return new EdgeDriverManager();
        if (browser.equals("safari")) return new SafariDriverManager();
        if (browser.contains("internet explorer") || browser.equals("ie")) return new IEDriverManager();
        throw new IllegalStateException("Unsupported local browser: " + browserName);
    }
}
