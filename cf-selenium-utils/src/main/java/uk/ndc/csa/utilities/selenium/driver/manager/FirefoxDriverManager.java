package uk.ndc.csa.utilities.selenium.driver.manager;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

public class FirefoxDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        if (Property.getVariable("cukes.driverPath") != null) {
            System.setProperty("webdriver.gecko.driver", getDriverPath("geckodriver"));
        }
        FirefoxOptions options = new FirefoxOptions();
        FrameworkProperties props = ThreadContext.getInstance().getSeleniumProps();
        String browserName = ThreadContext.getInstance().getBrowserName();
        String key = browserName == null ? "firefox" : browserName.replaceAll("\\s", "");
        options.addArguments(props.getStringArray("options." + key));
        options.merge(new Capabilities().getCap());
        driver = new FirefoxDriver(options);
    }
}
