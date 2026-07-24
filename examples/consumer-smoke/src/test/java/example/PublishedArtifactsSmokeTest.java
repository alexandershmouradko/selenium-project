package example;

import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.restapi.RestAssuredHelper;
import uk.ndc.csa.utilities.selenium.pageobjects.BasePO;

public class PublishedArtifactsSmokeTest {
    @Test
    public void publishedFrameworkModulesAreConsumable() {
        Assert.assertTrue(FrameworkProperties.empty().isEmpty());
        Assert.assertNotNull(RestAssuredHelper.class);
        Assert.assertNotNull(BasePO.class);
    }
}
