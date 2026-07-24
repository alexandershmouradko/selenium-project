package uk.ndc.csa.utilities.selenium.driver.factory;

import java.util.LinkedHashMap;
import java.util.Map;
import org.openqa.selenium.MutableCapabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import uk.ndc.csa.utilities.common.ThreadContext;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

public class CapabilitiesTest {

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        ThreadContext.clear();
    }

    @Test
    public void buildsW3cCapabilitiesFromLegacyBrowserAliases() {
        Map<String, String> browser = new LinkedHashMap<>();
        browser.put("seleniumServer", "local");
        browser.put("browser", "Chrome kiosk");
        browser.put("version", "144");
        browser.put("platform", "linux");
        browser.put("acceptInsecureCerts", "true");
        browser.put("vendor:buildNumber", "101");

        ThreadContext context = ThreadContext.getInstance();
        context.setSelenium(true);
        context.setBrowserCombo(browser);

        MutableCapabilities actual = new Capabilities().getCap();

        assertEquals(actual.getBrowserName(), "chrome");
        assertEquals(actual.getBrowserVersion(), "144");
        assertEquals(String.valueOf(actual.getPlatformName()), "linux");
        assertEquals(actual.getCapability("acceptInsecureCerts"), true);
        assertEquals(actual.getCapability("vendor:buildNumber"), 101);
        assertFalse(actual.asMap().containsKey("seleniumServer"));
        assertFalse(actual.asMap().containsKey("name"));
        assertFalse(actual.asMap().containsKey("build"));
    }

    @Test
    public void mapsBrowserStackLegacyOsAliasesOutsideStandardCapabilities() {
        ThreadContext context = ThreadContext.getInstance();
        context.setSelenium(true);
        context.setBrowserCombo(Map.of(
                "seleniumServer", "browserstack",
                "browser", "chrome",
                "browser_version", "latest",
                "os", "Windows",
                "os_version", "11"));

        MutableCapabilities actual = new Capabilities().getCap();

        assertEquals(actual.getBrowserName(), "chrome");
        assertEquals(actual.getBrowserVersion(), "latest");
        assertEquals(actual.getCapability("platformName"), null);
    }

    @Test
    public void rejectsUnnamespacedCustomCapabilities() {
        ThreadContext context = ThreadContext.getInstance();
        context.setSelenium(true);
        context.setBrowserCombo(Map.of(
                "browserName", "firefox",
                "customCapability", "value"));

        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                Capabilities::new);

        assertTrue(error.getMessage().contains("vendor-namespaced"));
    }
}
