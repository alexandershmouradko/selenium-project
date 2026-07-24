package uk.ndc.csa.utilities.common;

import io.cucumber.java.Scenario;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Cucumber-native logging and attachment facade retained for legacy step code. */
public final class Reporter {
    private static final ThreadLocal<Scenario> SCENARIO = new ThreadLocal<>();
    private static final ThreadLocal<String> ERROR_MESSAGE = new ThreadLocal<>();

    private Reporter() {
    }

    public static void setScenario(Scenario scenario) {
        SCENARIO.set(scenario);
    }

    public static void clearScenario() {
        SCENARIO.remove();
        ERROR_MESSAGE.remove();
    }

    public static void recordError(Throwable throwable) {
        ERROR_MESSAGE.set(throwable == null ? null : throwable.getMessage());
    }

    public static void addStepLog(String message) { log(message); }
    public static void addScenarioLog(String message) { log(message); }

    public static void log(String message) {
        Scenario scenario = SCENARIO.get();
        if (scenario != null) {
            scenario.log(String.valueOf(message));
        } else {
            System.out.println(message);
        }
    }

    public static void addScreenCaptureFromPath(String imagePath) throws IOException {
        addScreenCaptureFromPath(imagePath, "Screenshot");
    }

    public static void addScreenCaptureFromPath(String imagePath, String title) throws IOException {
        Scenario scenario = SCENARIO.get();
        if (scenario == null) {
            return;
        }
        byte[] bytes = Files.readAllBytes(Path.of(imagePath));
        scenario.attach(bytes, "image/png", title);
    }

    public static String getReportPath() {
        String defaultPath = System.getProperty("user.dir") + File.separator + "build" + File.separator + "reports" + File.separator;
        String configured = Property.getProperty("reportPath");
        return configured == null || configured.isBlank() ? defaultPath : configured;
    }

    public static String getReportName() {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "RunReport_" + formatter.format(new Date()) + ".html";
    }

    public static String getScreenshotPath() {
        return getReportPath() + "screenshots" + File.separator;
    }

    public static String getErrorMsg() {
        return ERROR_MESSAGE.get();
    }
}
