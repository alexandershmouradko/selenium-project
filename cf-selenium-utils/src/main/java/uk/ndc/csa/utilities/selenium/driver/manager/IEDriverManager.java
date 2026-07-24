package uk.ndc.csa.utilities.selenium.driver.manager;

import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

/** Internet Explorer was removed from the supported modern browser baseline. */
public class IEDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        throw new UnsupportedOperationException("Internet Explorer is not supported. Use Microsoft Edge.");
    }
}
