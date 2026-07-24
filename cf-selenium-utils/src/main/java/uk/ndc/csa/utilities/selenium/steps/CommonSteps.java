package uk.ndc.csa.utilities.selenium.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import uk.ndc.csa.utilities.common.JsonHelper;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.pageobjects.WireFrame;

/**
 * Reusable Cucumber steps for browser navigation and the legacy WireFrame layer.
 * Annotation-based step definitions are used instead of the deprecated Java 8 lambda DSL.
 */
@SuppressWarnings("deprecation")
public class CommonSteps extends BaseSteps {

    @When("^the browser is opened$")
    public void openBrowser() {
        getDriver().manage().window().maximize();
    }

    @When("^the application \"(.*)\"$")
    public void openApplication(String app) {
        getDriver().manage().window().maximize();
        String url = ThreadContext.getInstance().getEnvironmentProps().getString(app);
        log.debug("Navigating to url: {}", url);
        getDriver().get(url);
    }

    @When("^the url \"(.*)\"$")
    public void openUrl(String url) {
        getDriver().manage().window().maximize();
        log.debug("Navigating to url: {}", url);
        getDriver().get(url);
    }

    @When("^browser is navigated back$")
    public void navigateBack() {
        getDriver().navigate().back();
    }

    @When("^browser is navigated forward$")
    public void navigateForward() {
        getDriver().navigate().forward();
    }

    @When("^form actions are performed \\(page=(.*)\\)$")
    public void performPageActions(String page, DataTable data) {
        JSONObject json = JsonHelper.getJSONData("src/test/resources/pagestructures/" + page + ".json");
        WireFrame wireFrame = readyWireFrame();
        wireFrame.performAction(json, data.asMaps(String.class, String.class));
    }

    @When("^the form values are \\(page=(.*)\\)$")
    public void checkPageValues(String page, DataTable data) {
        JSONObject json = JsonHelper.getJSONData("src/test/resources/pagestructures/" + page + ".json");
        WireFrame wireFrame = readyWireFrame();
        wireFrame.checkValue(json, data.asMaps(String.class, String.class));
    }

    @When("^form data is populated \\(by=(.*)\\)$")
    public void populateByLocatorMap(String uiMap, DataTable data) {
        WireFrame wireFrame = readyWireFrame();
        wireFrame.updateByValue(uiMap, data.asMaps(String.class, String.class));
    }

    @When("^the form values are \\(by=(.*)\\)$")
    public void checkByLocatorMap(String uiMap, DataTable data) {
        WireFrame wireFrame = readyWireFrame();
        wireFrame.checkByValue(uiMap, data.asMaps(String.class, String.class));
    }

    @When("^form data is populated \\(el=(.*)\\)$")
    public void populateByElementMethods(String uiMap, DataTable data) {
        WireFrame wireFrame = readyWireFrame();
        wireFrame.updateElementValue(uiMap, data.asMaps(String.class, String.class));
    }

    @When("^the form values are \\(el=(.*)\\)$")
    public void checkByElementMethods(String uiMap, DataTable data) {
        WireFrame wireFrame = readyWireFrame();
        wireFrame.checkElementValue(uiMap, data.asMaps(String.class, String.class));
    }

    private WireFrame readyWireFrame() {
        WireFrame wireFrame = new WireFrame();
        wireFrame.waitPageToLoad();
        return wireFrame;
    }
}
