package uk.ndc.csa.utilities.common;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ThreadContextTest {

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        ThreadContext.clear();
    }

    @Test
    public void contextIsIsolatedBetweenThreads() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<String> chrome = () -> contextValue("chrome", "first");
            Callable<String> firefox = () -> contextValue("firefox", "second");

            Future<String> first = executor.submit(chrome);
            Future<String> second = executor.submit(firefox);

            Assert.assertEquals(first.get(), "chrome:first");
            Assert.assertEquals(second.get(), "firefox:second");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void clearCreatesFreshContext() {
        ThreadContext original = ThreadContext.getInstance();
        original.testdataPut("key", "value");
        ThreadContext.clear();

        ThreadContext replacement = ThreadContext.getInstance();
        Assert.assertNotSame(replacement, original);
        Assert.assertNull(replacement.testdataGet("key"));
    }

    @Test
    public void browserCombinationIsDefensivelyCopied() {
        java.util.HashMap<String, String> browser = new java.util.HashMap<>();
        browser.put("browserName", "chrome");
        ThreadContext.getInstance().setBrowserCombo(browser);
        browser.put("browserName", "firefox");

        Assert.assertEquals(ThreadContext.getInstance().getBrowserName(), "chrome");
    }

    private static String contextValue(String browserName, String marker) {
        try {
            ThreadContext context = ThreadContext.getInstance();
            context.setBrowserCombo(Map.of("browserName", browserName));
            context.testdataPut("marker", marker);
            return context.getBrowserName() + ":" + context.testdataGet("marker");
        } finally {
            ThreadContext.clear();
        }
    }
}
