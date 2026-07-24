package uk.ndc.csa.utilities.har;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverFactory;

/**
 * Captures Chrome/Edge DevTools performance events without the abandoned
 * BrowserMob Proxy dependency. The output is a JSON network-event log that can
 * be converted to HAR by downstream tooling.
 */
public final class HarHelper {
    private boolean started;

    public void start() {
        started = true;
        ThreadContext.getInstance().testdataPut("networkCaptureStarted", System.currentTimeMillis());
    }

    public void close(String name) {
        if (!started) {
            return;
        }
        String configuredPath = ThreadContext.getInstance().getPathsProps()
                .getString("harPath", "/build/network");
        Path output = Path.of(System.getProperty("user.dir") + configuredPath, name + ".network.json");
        try {
            Files.createDirectories(output.getParent());
            JSONArray events = new JSONArray();
            for (LogEntry entry : performanceEntries()) {
                events.put(new JSONObject()
                        .put("timestamp", entry.getTimestamp())
                        .put("level", entry.getLevel().getName())
                        .put("message", parseMessage(entry.getMessage())));
            }
            Files.writeString(output, events.toString(2), StandardCharsets.UTF_8);
            ThreadContext.getInstance().testdataPut("networkCapturePath", output.toString());
        } catch (RuntimeException | IOException e) {
            throw new IllegalStateException(
                    "Unable to write Selenium performance log. Enable performance logging for Chrome/Edge.", e);
        } finally {
            started = false;
        }
    }

    private List<LogEntry> performanceEntries() {
        return new ArrayList<>(DriverFactory.getInstance().getDriver()
                .manage().logs().get(LogType.PERFORMANCE).getAll());
    }

    private static Object parseMessage(String value) {
        try {
            return new JSONObject(value);
        } catch (RuntimeException ignored) {
            return value;
        }
    }
}
