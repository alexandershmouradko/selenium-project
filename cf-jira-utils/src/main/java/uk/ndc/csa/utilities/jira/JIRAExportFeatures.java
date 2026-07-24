package uk.ndc.csa.utilities.jira;

import org.testng.annotations.Test;

/** Entry point retained for compatibility; see JIRAHelper.buildFeatureFiles(). */
public final class JIRAExportFeatures {
    @Test
    public void jiraFeatures() {
        new JIRAHelper().buildFeatureFiles();
    }
}
