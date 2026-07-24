package uk.ndc.csa.utilities.selenium.driver.manager;

import uk.ndc.csa.utilities.selenium.driver.factory.DriverManager;

/**
 * Compatibility placeholder for the legacy Perfecto integration. Modern Perfecto web/mobile
 * sessions require account-specific W3C/Appium capabilities and must be implemented in a
 * dedicated provider module rather than sending legacy top-level user/password capabilities.
 */
public class PerfectoDriverManager extends DriverManager {
    @Override
    protected void createDriver() {
        throw new UnsupportedOperationException(
                "Perfecto is not enabled in the 3.0 core. Add a dedicated W3C/Appium provider module.");
    }
}
