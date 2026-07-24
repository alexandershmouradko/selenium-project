package uk.ndc.csa.utilities.common;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** Global scenario lifecycle hooks. */
public class CucumberHooks {
    @Before(order = Integer.MIN_VALUE)
    public void beforeScenario(Scenario scenario) {
        Reporter.setScenario(scenario);
        ThreadContext context = ThreadContext.getInstance();
        context.setFeature(scenario.getUri() == null ? "unknown-feature" : scenario.getUri().toString());
        context.setScenario(scenario.getName());
    }

    @After(order = Integer.MIN_VALUE)
    public void afterScenario(Scenario scenario) {
        AssertionError assertionError = null;
        try {
            ThreadContext.getInstance().sa().assertAll();
        } catch (AssertionError error) {
            assertionError = error;
            Reporter.recordError(error);
            scenario.log(error.getMessage() == null ? "Soft assertion failed" : error.getMessage());
        } finally {
            Reporter.clearScenario();
            ThreadContext.clear();
        }
        if (assertionError != null) {
            throw assertionError;
        }
    }
}
