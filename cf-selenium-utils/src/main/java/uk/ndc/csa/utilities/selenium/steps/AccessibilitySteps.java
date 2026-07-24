package uk.ndc.csa.utilities.selenium.steps;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.testng.Assert;
import uk.ndc.csa.utilities.common.Property;

/** Accessibility scanning based on the maintained Deque AxeBuilder API. */
public class AccessibilitySteps extends BaseSteps {

    public String getAccessibilityReportPath() {
        String configured = Property.getProperty("accessibilityReportPath");
        return configured == null || configured.isBlank()
                ? Path.of(System.getProperty("user.dir"), "build", "accessibility").toString()
                : configured;
    }

    public Path generateReportFile() {
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
        Path directory = Path.of(getAccessibilityReportPath());
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create accessibility report directory", e);
        }
        return directory.resolve("AccessibilityViolations_" + timestamp + ".json");
    }

    public void determineViolations(List<Rule> violations) {
        determineViolations(getDriver().getCurrentUrl(), violations);
    }

    public void determineViolations(String pageUrl, List<Rule> violations) {
        if (violations == null || violations.isEmpty()) {
            return;
        }
        Path report = generateReportFile();
        try {
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(violations);
            Files.writeString(report, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write accessibility report", e);
        }
        Assert.fail("Accessibility violations found on " + pageUrl + ": " + violations.size()
                + ". Report: " + report);
    }

    public void testAccessibility(String pageUrl) {
        getDriver().get(pageUrl);
        Results results = new AxeBuilder().analyze(getDriver());
        determineViolations(pageUrl, results.getViolations());
    }

    public void testAccessibilityWithOptions(String pageUrl) {
        getDriver().get(pageUrl);
        Results results = new AxeBuilder()
                .disableRules(List.of("accesskeys"))
                .analyze(getDriver());
        determineViolations(pageUrl, results.getViolations());
    }

    public void testAccessibilityWithIncludesAndExcludes(
            String pageUrl, String includedElement, String excludedElement) {
        getDriver().get(pageUrl);
        Results results = new AxeBuilder()
                .include(includedElement)
                .exclude(excludedElement)
                .analyze(getDriver());
        determineViolations(pageUrl, results.getViolations());
    }

    public void testAccessibilityWithWebElement(String pageUrl, String tag) {
        getDriver().get(pageUrl);
        Results results = new AxeBuilder()
                .include(tag)
                .analyze(getDriver());
        determineViolations(pageUrl, results.getViolations());
    }
}
