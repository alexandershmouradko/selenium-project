package uk.ndc.csa.utilities.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Cucumber 7 + TestNG base runner with scenario-level parallelism and the
 * original browser-combination matrix concept.
 */
@CucumberOptions(
        plugin = {
                "pretty",
                "json:build/cucumber/cucumber.json",
                "html:build/cucumber/cucumber.html"
        },
        glue = {
                "uk.ndc.csa.utilities.common",
                "uk.ndc.csa.utilities.selenium",
                "uk.ndc.csa.utilities.restapi",
                "uk.ndc.csa.utilities.mockservice",
                "uk.ndc.csa.utilities.jira"
        }
)
public abstract class BaseTest {
    private TestNGCucumberRunner cucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        cucumberRunner = new TestNGCucumberRunner(getClass());
    }

    @Test(groups = "cucumber", dataProvider = "scenarios")
    public void runScenario(PickleWrapper pickle, FeatureWrapper feature, Map<String, String> browserCombo) {
        boolean selenium = Boolean.parseBoolean(System.getProperty("cukes.selenium", "false"));
        ThreadContext.getInstance().setThreadContext(selenium, browserCombo);
        cucumberRunner.runScenario(pickle.getPickle());
    }

    @DataProvider(name = "scenarios", parallel = true)
    public Object[][] scenarios() {
        Object[][] cucumberScenarios = cucumberRunner.provideScenarios();
        List<Map<String, String>> browserCombinations = loadBrowserCombinations();
        Object[][] matrix = new Object[cucumberScenarios.length * browserCombinations.size()][3];
        int index = 0;
        for (Object[] scenario : cucumberScenarios) {
            for (Map<String, String> browser : browserCombinations) {
                matrix[index][0] = scenario[0];
                matrix[index][1] = scenario[1];
                matrix[index][2] = browser;
                index++;
            }
        }
        return matrix;
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (cucumberRunner != null) {
            cucumberRunner.finish();
        }
        TextReport report = new TextReport();
        report.generateReport();
        String path = Property.getProperty("textReportPath");
        if (path != null && !path.isBlank()) {
            report.saveReport(path);
        }
    }

    private List<Map<String, String>> loadBrowserCombinations() {
        if (!Boolean.parseBoolean(System.getProperty("cukes.selenium", "false"))) {
            return List.of(Map.of());
        }

        String browserCombo = Property.requireVariable("cukes.browsercombo");
        String configuredPath = Property.getProperty("seleniumStackPath");
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = "/src/test/resources/config/selenium/stacks/";
        }
        Path path = Path.of(System.getProperty("user.dir") + configuredPath + browserCombo + ".json");
        JSONArray array = JsonHelper.getJSONArray(path.toString());
        Type mapType = new TypeToken<LinkedHashMap<String, String>>() { }.getType();
        Gson gson = new Gson();
        List<Map<String, String>> combinations = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            combinations.add(gson.fromJson(array.getJSONObject(i).toString(), mapType));
        }
        if (combinations.isEmpty()) {
            throw new IllegalStateException("Browser combination file is empty: " + path);
        }
        return combinations;
    }
}
