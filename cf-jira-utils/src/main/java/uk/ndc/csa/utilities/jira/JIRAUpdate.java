package uk.ndc.csa.utilities.jira;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ZipHelper;
import uk.ndc.csa.utilities.jira.JIRAHelper.JiraIssue;

/** Publishes accumulated Cucumber results to JIRA after the execution suite. */
public final class JIRAUpdate {

    @Test
    public void jiraIssues() throws Exception {
        Map<String, List<Map<String, Object>>> results = JIRAContext.getIssues();
        int bugLevel = integerVariable("cukes.jira.bugLevel", 0);
        boolean runFailed = false;
        JiraIssue runBug = null;
        Map<String, Boolean> featureFailures = new HashMap<>();
        JIRAHelper jira = new JIRAHelper();

        for (Map.Entry<String, List<Map<String, Object>>> entry : results.entrySet()) {
            String scenarioKey = entry.getKey();
            boolean scenarioFailed = entry.getValue().stream()
                    .anyMatch(item -> Boolean.parseBoolean(String.valueOf(item.get("failed"))));
            runFailed |= scenarioFailed;

            String featureKey = entry.getValue().stream()
                    .map(item -> String.valueOf(item.getOrDefault("jiraFeature", "")))
                    .filter(value -> !value.isBlank())
                    .findFirst().orElse("");
            if (!featureKey.isBlank()) {
                featureFailures.merge(featureKey, scenarioFailed, Boolean::logicalOr);
            }

            String comment = entry.getValue().stream()
                    .map(item -> String.valueOf(item.getOrDefault("comment", "")))
                    .reduce("", String::concat);
            jira.addRunResult(scenarioKey, scenarioFailed, comment);

            JiraIssue scenarioBug = null;
            if (scenarioFailed && bugLevel == 2) {
                Map<String, Object> firstFailure = entry.getValue().stream()
                        .filter(item -> Boolean.parseBoolean(String.valueOf(item.get("failed"))))
                        .findFirst().orElse(Map.of());
                scenarioBug = jira.addScenarioBug(
                        scenarioKey,
                        String.valueOf(firstFailure.getOrDefault("bugSummary", "Automated test failure")),
                        String.valueOf(firstFailure.getOrDefault("bugDescription", comment)),
                        nullableString(firstFailure.get("screenshotPath")));
            }
            if (scenarioFailed && scenarioBug != null) {
                jira.linkIssues(scenarioBug.key(), scenarioKey, "Blocks");
            }
        }

        if (runFailed && bugLevel == 1) {
            runBug = jira.addRunBug("Automated Test Run Failure",
                    "Please see the attached run report for test failure details.");
            ZipHelper.zipit("RunReports", "RunReport.zip");
            File report = new File("RunReport.zip");
            if (report.isFile()) {
                jira.addIssueAttachment(runBug, report, report.getName());
            }
        }

        for (Map.Entry<String, Boolean> feature : featureFailures.entrySet()) {
            jira.addRunResult(feature.getKey(), feature.getValue(), null);
        }
        JIRAContext.clear();
    }

    private static int integerVariable(String key, int defaultValue) {
        try {
            String value = Property.getVariable(key);
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static String nullableString(Object value) {
        return value == null ? null : value.toString();
    }
}
