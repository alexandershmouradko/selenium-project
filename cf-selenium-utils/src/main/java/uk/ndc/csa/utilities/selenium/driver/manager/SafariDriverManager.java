package uk.ndc.csa.utilities.selenium.driver.manager;

import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

public class SafariDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        SafariOptions options = new SafariOptions();
        options.merge(new Capabilities().getCap());
        driver = new SafariDriver(options);
    }
}
