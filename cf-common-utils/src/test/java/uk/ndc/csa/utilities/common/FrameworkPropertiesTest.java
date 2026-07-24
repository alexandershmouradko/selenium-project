package uk.ndc.csa.utilities.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FrameworkPropertiesTest {

    @Test
    public void loadsTypedValuesAndEscapedLists() throws Exception {
        Path file = Files.createTempFile("framework", ".properties");
        Files.writeString(file, String.join(System.lineSeparator(),
                "name=local",
                "threads=4",
                "enabled=true",
                "items=chrome, firefox,edge\\\\,beta"));

        FrameworkProperties properties = FrameworkProperties.load(file);

        Assert.assertEquals(properties.getString("name"), "local");
        Assert.assertEquals(properties.getInt("threads"), 4);
        Assert.assertTrue(properties.getBoolean("enabled"));
        Assert.assertEquals(List.of(properties.getStringArray("items")),
                List.of("chrome", "firefox", "edge,beta"));
        Assert.assertEquals(properties.getInt("missing", 7), 7);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsInvalidInteger() throws Exception {
        Path file = Files.createTempFile("framework-invalid", ".properties");
        Files.writeString(file, "threads=not-a-number");
        FrameworkProperties.load(file).getInt("threads");
    }
}
