package uk.ndc.csa.utilities.jira;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.Reporter;
import uk.ndc.csa.utilities.common.ThreadContext;

/** Collects scenario results for optional asynchronous JIRA publication. */
public final class CucumberHooks {

    @After(order = 20)
    public void collectResult(Scenario scenario) {
        if (!Boolean.parseBoolean(Property.getVariable("cukes.jira"))) {
            return;
        }

        Collection<String> tags = scenario.getSourceTagNames();
        Optional<String> jiraScenario = jiraRef(tags, "Scenario:");
        if (jiraScenario.isEmpty()) {
            Reporter.log("JIRA update skipped: no @Scenario:<KEY> tag on " + scenario.getName());
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("jiraStory", jiraRef(tags, "Story:").orElse(""));
        result.put("jiraFeature", jiraRef(tags, "Feature:").orElse(""));
        result.put("jiraScenario", jiraScenario.get());
        result.put("failed", scenario.isFailed());

        StringBuilder details = new StringBuilder()
                .append("---------------------------\n")
                .append("Feature: ").append(nullToEmpty(ThreadContext.getInstance().getFeature())).append('\n')
                .append("Scenario: ").append(scenario.getName()).append('\n');

        if (ThreadContext.getInstance().getSelenium()) {
            details.append("Browser: ")
                    .append(nullToEmpty(ThreadContext.getInstance().getBrowserName())).append(' ')
                    .append(nullToEmpty(ThreadContext.getInstance().getBrowserVersion())).append(' ')
                    .append(nullToEmpty(ThreadContext.getInstance().getPlatform())).append('\n');
        }

        details.append("Environment: ").append(nullToEmpty(Property.getVariable("cukes.env"))).append('\n')
                .append("Build: ").append(nullToEmpty(Property.getVariable("JOB_NAME"))).append('_')
                .append(nullToEmpty(Property.getVariable("BUILD_NUMBER"))).append('\n')
                .append("Status: ").append(scenario.isFailed() ? "Failed" : "Passed").append('\n')
                .append("---------------------------\n");

        result.put("comment", details.toString());
        int bugLevel = integerVariable("cukes.jira.bugLevel", 0);
        result.put("bugLevel", bugLevel);

        if (scenario.isFailed()) {
            result.put("bugSummary", "Automated Test Failure for Scenario: " + scenario.getName());
            result.put("bugDescription", details + "Error: " + nullToEmpty(Reporter.getErrorMsg()) + '\n');
            Object screenshot = ThreadContext.getInstance().testdataGet("screenshotPath");
            if (screenshot != null) {
                result.put("screenshotPath", screenshot.toString());
            }
        }

        JIRAContext.addIssue(jiraScenario.get(), result);
    }

    private static Optional<String> jiraRef(Collection<String> tags, String prefix) {
        String marker = "@" + prefix;
        return tags.stream()
                .filter(tag -> tag.regionMatches(true, 0, marker, 0, marker.length()))
                .map(tag -> tag.substring(marker.length()).trim())
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private static int integerVariable(String key, int defaultValue) {
        try {
            String value = Property.getVariable(key);
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
