package uk.ndc.csa.utilities.selenium.driver.manager;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import java.util.logging.Level;
import org.openqa.selenium.edge.EdgeOptions;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

public class EdgeDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        if (Property.getVariable("cukes.driverPath") != null) {
            System.setProperty("webdriver.edge.driver", getDriverPath("msedgedriver"));
        }
        EdgeOptions options = new EdgeOptions();
        if (Boolean.parseBoolean(Property.getVariable("cukes.networkCapture"))) {
            LoggingPreferences logging = new LoggingPreferences();
            logging.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("ms:loggingPrefs", logging);
        }
        options.merge(new Capabilities().getCap());
        driver = new EdgeDriver(options);
    }
}
