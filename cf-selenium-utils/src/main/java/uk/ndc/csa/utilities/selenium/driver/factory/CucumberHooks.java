package uk.ndc.csa.utilities.selenium.driver.factory;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import java.io.File;
import org.openqa.selenium.JavascriptExecutor;
import uk.ndc.csa.utilities.common.Reporter;
import uk.ndc.csa.utilities.common.ThreadContext;

import static uk.ndc.csa.utilities.selenium.screenshot.Screenshot.grabScreenshot;
import static uk.ndc.csa.utilities.selenium.screenshot.Screenshot.saveScreenshot;

/** UI cleanup and failure diagnostics. */
public class CucumberHooks {
    @After(order = 30)
    public void afterUiScenario(Scenario scenario) {
        ThreadContext context = ThreadContext.getInstance();
        if (!context.getSelenium()) return;

        try {
            if (scenario.isFailed() && context.getSeleniumProps().getBoolean("screenshotOnFailure", true)) {
                File screenshot = saveScreenshot(grabScreenshot(), Reporter.getScreenshotPath());
                Reporter.addScreenCaptureFromPath(screenshot.getAbsolutePath(), "Failure screenshot");
                context.testdataPut("screenshotPath", screenshot.getAbsolutePath());
            }

            String server = context.getBrowserCombo().getOrDefault("seleniumServer", "local");
            if ("saucelabs".equalsIgnoreCase(server)) {
                ((JavascriptExecutor) DriverFactory.getInstance().getDriver())
                        .executeScript("sauce:job-result=" + (scenario.isFailed() ? "failed" : "passed"));
            }
        } catch (Exception diagnosticFailure) {
            Reporter.log("UI diagnostic collection failed: " + diagnosticFailure.getMessage());
        } finally {
            if (!context.getKeepBrowserOpen()) {
                DriverFactory.getInstance().quit();
            }
        }
    }
}
