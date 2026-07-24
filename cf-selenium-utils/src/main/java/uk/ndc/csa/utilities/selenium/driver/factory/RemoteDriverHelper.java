package uk.ndc.csa.utilities.selenium.driver.factory;

import java.net.URL;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

/** Selenium 4 remote-driver creation helper. Proxying is configured via JVM/network settings. */
public final class RemoteDriverHelper {
    private RemoteDriverHelper() {
    }

    public static RemoteWebDriver getRemoteDriver(URL url, Capabilities capabilities) {
        RemoteWebDriver driver = new RemoteWebDriver(url, capabilities);
        driver.setFileDetector(new LocalFileDetector());
        return driver;
    }
}
