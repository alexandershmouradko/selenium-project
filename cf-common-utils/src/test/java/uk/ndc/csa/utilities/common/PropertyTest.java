package uk.ndc.csa.utilities.common;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class PropertyTest {
    private static final String KEY = "cukes.test.variable";

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        System.clearProperty(KEY);
    }

    @Test
    public void systemPropertyHasPriority() {
        System.setProperty(KEY, "system-value");
        Assert.assertEquals(Property.getVariable(KEY), "system-value");
        Assert.assertEquals(Property.requireVariable(KEY), "system-value");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void requireVariableRejectsMissingValue() {
        System.clearProperty(KEY);
        Property.requireVariable(KEY);
    }
}
