package uk.ndc.csa.utilities.common;

import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TextReportTest {

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        System.clearProperty("textReport.jsonPath");
    }

    @Test
    public void summarizesPassedAndFailedScenariosAndCanBeRegenerated() throws Exception {
        Path cucumberJson = Files.createTempFile("cucumber", ".json");
        Files.writeString(cucumberJson, """
                [{
                  "name":"Login",
                  "elements":[
                    {"type":"scenario","name":"valid login","steps":[
                      {"result":{"status":"passed","duration":1000000000}}
                    ]},
                    {"type":"scenario","name":"invalid login","steps":[
                      {"result":{"status":"failed","duration":500000000}}
                    ]}
                  ]
                }]
                """);
        System.setProperty("textReport.jsonPath", cucumberJson.toString());

        TextReport report = new TextReport();
        report.generateReport();
        String first = report.getReport();
        report.generateReport();
        String second = report.getReport();

        Assert.assertEquals(second, first, "Regeneration must not duplicate counters or output");
        Assert.assertTrue(first.contains("Features: 1 (PASS 0, FAIL 1, SKIP 0)"));
        Assert.assertTrue(first.contains("Scenarios: 2 (PASS 1, FAIL 1, SKIP 0)"));

        Path output = Files.createTempDirectory("text-report").resolve("nested/report.txt");
        report.saveReport(output.toString());
        Assert.assertEquals(Files.readString(output), first);
    }

    @Test
    public void reportsMissingJsonWithoutThrowing() {
        System.setProperty("textReport.jsonPath", "/path/that/does/not/exist.json");
        TextReport report = new TextReport();
        report.generateReport();
        Assert.assertTrue(report.getReport().contains("Cucumber JSON not found"));
    }
}
