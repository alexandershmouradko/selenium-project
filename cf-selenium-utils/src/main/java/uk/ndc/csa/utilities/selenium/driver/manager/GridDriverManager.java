package uk.ndc.csa.utilities.selenium.driver.manager;

import java.net.MalformedURLException;
import java.net.URL;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.selenium.driver.factory.Capabilities;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;
import uk.ndc.csa.utilities.selenium.driver.factory.RemoteDriverHelper;

public class GridDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        try {
            driver = RemoteDriverHelper.getRemoteDriver(
                    new URL(Property.requireVariable("cukes.seleniumGrid")),
                    new Capabilities().getCap());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Selenium Grid URL", e);
        }
    }
}
