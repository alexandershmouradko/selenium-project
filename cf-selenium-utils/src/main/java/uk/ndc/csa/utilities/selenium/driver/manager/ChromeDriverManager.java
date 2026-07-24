package uk.ndc.csa.utilities.selenium.driver.manager;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import java.util.logging.Level;
import org.openqa.selenium.chrome.ChromeOptions;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

public class ChromeDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        if (Property.getVariable("cukes.driverPath") != null) {
            System.setProperty("webdriver.chrome.driver", getDriverPath("chromedriver"));
        }
        ChromeOptions options = new ChromeOptions();
        FrameworkProperties props = ThreadContext.getInstance().getSeleniumProps();
        String browserName = ThreadContext.getInstance().getBrowserName();
        String key = browserName == null ? "chrome" : browserName.replaceAll("\\s", "");
        options.addArguments(props.getStringArray("options." + key));

        if (browserName != null && browserName.contains("kiosk")) options.addArguments("--kiosk");
        if (browserName != null && browserName.contains("mobile-emulator")) {
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", ThreadContext.getInstance().getBrowserCombo().get("deviceName"));
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }
        if (Boolean.parseBoolean(Property.getVariable("cukes.networkCapture"))) {
            LoggingPreferences logging = new LoggingPreferences();
            logging.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", logging);
        }
        options.merge(new Capabilities().getCap());
        driver = new ChromeDriver(options);
    }
}
