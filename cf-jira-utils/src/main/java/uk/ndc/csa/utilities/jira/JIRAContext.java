package uk.ndc.csa.utilities.jira;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe accumulator for results that are published after a test run. */
public final class JIRAContext {
    private static final Map<String, List<Map<String, Object>>> ISSUES = new ConcurrentHashMap<>();

    private JIRAContext() {
    }

    public static void addIssue(String key, Map<String, Object> data) {
        if (key == null || key.isBlank()) {
            return;
        }
        ISSUES.computeIfAbsent(key, ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(Map.copyOf(data));
    }

    public static Map<String, List<Map<String, Object>>> getIssues() {
        return Collections.unmodifiableMap(ISSUES);
    }

    public static Set<String> keys() {
        return ISSUES.keySet();
    }

    public static void clear() {
        ISSUES.clear();
    }
}
