package uk.ndc.csa.utilities.selenium.driver.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;

import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ThreadContext;

public abstract class DriverManager {
	
	protected WebDriver driver;
	protected WebDriverWait wait;
	
	public WebDriver getDriver(){
		if (driver == null){
			createDriver();
		}
		return driver;
	}
	
	public void quitDriver(){
		if (driver != null){
			driver.quit();
			driver = null;
		}
	}
	
	public WebDriverWait getWait() {
		if (wait == null){
			wait = new WebDriverWait(getDriver(), Duration.ofSeconds(getWaitDuration()));
		}
		return wait;
	}
	
    public String getDriverPath(String driverName) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        String executable = driverName + (windows ? ".exe" : "");
        String configured = Property.getVariable("cukes.driverPath");
        if (configured == null || configured.isBlank()) {
            String osFamily = windows ? "windows" : System.getProperty("os.name", "linux")
                    .split(" ")[0].toLowerCase();
            return Path.of("lib", "drivers", osFamily, executable).toString();
        }
        Path path = Path.of(configured);
        if (Files.isRegularFile(path) || path.getFileName().toString().equalsIgnoreCase(executable)) {
            return path.toString();
        }
        String osFamily = windows ? "windows" : System.getProperty("os.name", "linux")
                .split(" ")[0].toLowerCase();
        return path.resolve(osFamily).resolve(executable).toString();
    }
	
	/** Returns duration for specified waits */
	public int getWaitDuration(){		
		final int defaultWait = 10;	
		int duration;
		try {
			duration = ThreadContext.getInstance().getEnvironmentProps().getInt("defaultWait");
		} catch (Exception e) {
			duration = defaultWait;
		}
		return duration; 
	}
	
	protected abstract void createDriver();
	
	
} 