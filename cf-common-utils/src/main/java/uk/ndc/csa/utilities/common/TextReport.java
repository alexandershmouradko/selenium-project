package uk.ndc.csa.utilities.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/**
 * Lightweight text summary based on Cucumber JSON report.
 *
 * Expected JSON report path: build/cucumber/cucumber.json (override with -DtextReport.jsonPath)
 */
public class TextReport {

    private final StringBuilder sb = new StringBuilder();

    private int countFeature;
    private int countFeatureFail;
    private int countFeaturePass;
    private int countFeatureSkip;

    private int countScenario;
    private int countScenarioFail;
    private int countScenarioPass;
    private int countScenarioSkip;

    public void generateReport() {
        reset();
        String jsonPath = System.getProperty("textReport.jsonPath",
                System.getProperty("user.dir") + File.separator + "build" + File.separator + "cucumber" + File.separator + "cucumber.json");

        File f = new File(jsonPath);
        if (!f.isFile()) {
            sb.append("Cucumber JSON not found: ").append(jsonPath).append("\n");
            sb.append("Tip: add plugin \"json:build/cucumber/cucumber.json\" to @CucumberOptions.\n");
            return;
        }

        try {
            String raw = Files.readString(f.toPath(), StandardCharsets.UTF_8);
            JSONArray features = new JSONArray(raw);

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                String featureName = feature.optString("name", "Unnamed Feature");

                JSONArray elements = feature.optJSONArray("elements");
                if (elements == null) elements = new JSONArray();

                FeatureStatus fs = FeatureStatus.PASS;
                long featureDurationNs = 0L;

                for (int j = 0; j < elements.length(); j++) {
                    JSONObject scenario = elements.getJSONObject(j);
                    String type = scenario.optString("type", "");
                    if (!type.equalsIgnoreCase("scenario") && !type.equalsIgnoreCase("scenario_outline")) {
                        continue;
                    }

                    String scenarioName = scenario.optString("name", "Unnamed Scenario");
                    JSONArray steps = scenario.optJSONArray("steps");
                    if (steps == null) steps = new JSONArray();

                    ScenarioStatus ss = ScenarioStatus.PASS;
                    long scenarioDurationNs = 0L;

                    for (int k = 0; k < steps.length(); k++) {
                        JSONObject step = steps.getJSONObject(k);
                        JSONObject result = step.optJSONObject("result");
                        if (result == null) continue;

                        String status = result.optString("status", "passed").toLowerCase(Locale.ROOT);
                        long dur = result.optLong("duration", 0L); // usually in ns
                        scenarioDurationNs += dur;

                        if (status.equals("failed")) ss = ScenarioStatus.FAIL;
                        else if ((status.equals("skipped") || status.equals("pending") || status.equals("undefined"))
                                && ss != ScenarioStatus.FAIL) ss = ScenarioStatus.SKIP;
                    }

                    // counts
                    countScenario++;
                    if (ss == ScenarioStatus.PASS) countScenarioPass++;
                    if (ss == ScenarioStatus.FAIL) countScenarioFail++;
                    if (ss == ScenarioStatus.SKIP) countScenarioSkip++;

                    // feature status is worst-case
                    if (ss == ScenarioStatus.FAIL) fs = FeatureStatus.FAIL;
                    else if (ss == ScenarioStatus.SKIP && fs != FeatureStatus.FAIL) fs = FeatureStatus.SKIP;

                    featureDurationNs += scenarioDurationNs;

                    sb.append(featureName).append(" -> ").append(scenarioName)
                            .append(" | ").append(ss)
                            .append(" | ").append(formatDuration(scenarioDurationNs))
                            .append("\n");
                }

                countFeature++;
                if (fs == FeatureStatus.PASS) countFeaturePass++;
                if (fs == FeatureStatus.FAIL) countFeatureFail++;
                if (fs == FeatureStatus.SKIP) countFeatureSkip++;

                sb.append("Feature Summary: ").append(featureName)
                        .append(" | ").append(fs)
                        .append(" | ").append(formatDuration(featureDurationNs))
                        .append("\n\n");
            }

            sb.append("Totals:\n");
            sb.append("Features: ").append(countFeature)
                    .append(" (PASS ").append(countFeaturePass)
                    .append(", FAIL ").append(countFeatureFail)
                    .append(", SKIP ").append(countFeatureSkip)
                    .append(")\n");
            sb.append("Scenarios: ").append(countScenario)
                    .append(" (PASS ").append(countScenarioPass)
                    .append(", FAIL ").append(countScenarioFail)
                    .append(", SKIP ").append(countScenarioSkip)
                    .append(")\n");

        } catch (Exception e) {
            sb.append("Failed to parse Cucumber JSON: ").append(e.getMessage()).append("\n");
        }
    }

    public void printReport() {
        System.out.println(sb);
    }

    public String getReport() {
        return sb.toString();
    }

    public void saveReport(String path) {
        try {
            File target = new File(path);
            File parent = target.getParentFile();
            if (parent != null) {
                Files.createDirectories(parent.toPath());
            }
            try (PrintWriter out = new PrintWriter(target, StandardCharsets.UTF_8)) {
                out.print(sb);
            }
        } catch (IOException e) {
            System.out.println("Could not save text report: " + e.getMessage());
        }
    }

    private void reset() {
        sb.setLength(0);
        countFeature = 0;
        countFeatureFail = 0;
        countFeaturePass = 0;
        countFeatureSkip = 0;
        countScenario = 0;
        countScenarioFail = 0;
        countScenarioPass = 0;
        countScenarioSkip = 0;
    }

    private String formatDuration(long ns) {
        double seconds = ns / 1_000_000_000.0;
        return String.format(Locale.ROOT, "%.2fs", seconds);
    }

    private enum ScenarioStatus { PASS, FAIL, SKIP }
    private enum FeatureStatus { PASS, FAIL, SKIP }
}
