package uk.ndc.csa.utilities.jira;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.JsonHelper;
import uk.ndc.csa.utilities.common.Property;

/**
 * Lightweight JIRA REST client that replaces the retired Atlassian async Java
 * client. Supports transitions, comments, issue creation, linking and attachments.
 */
public final class JIRAHelper {
    private final HttpClient client;
    private final URI baseUri;
    private final FrameworkProperties properties;
    private final String authorization;
    private final String apiVersion;

    public JIRAHelper() {
        String jiraPath = Objects.requireNonNullElse(Property.getProperty("jiraPath"),
                "/src/test/resources/config/jira/");
        this.properties = Property.getProperties(System.getProperty("user.dir") + jiraPath + "jira.properties");
        this.baseUri = URI.create(require(properties, "uri").replaceAll("/$", ""));
        this.apiVersion = properties.getString("apiVersion", "2");
        this.authorization = createAuthorization(properties);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void addRunResult(String jiraRef, Boolean failed, String comment) throws IOException, InterruptedException {
        transitionIssue(new JiraIssue(jiraRef), Boolean.TRUE.equals(failed) ? "Test Failed" : "Test Passed", comment);
    }

    public JiraIssue addScenarioBug(String jiraScenario, String summary, String description, String screenshotPath)
            throws IOException, InterruptedException {
        JiraIssue issue = getExistingBug(summary, description).orElseGet(() -> {
            try {
                return createBug(summary, description);
            } catch (IOException | InterruptedException e) {
                throw new JiraOperationException(e);
            }
        });
        if (screenshotPath != null && !screenshotPath.isBlank()) {
            File screenshot = new File(screenshotPath);
            if (screenshot.isFile()) {
                addIssueAttachment(issue, screenshot, screenshot.getName());
            }
        }
        return issue;
    }

    public JiraIssue addRunBug(String summary, String description) throws IOException, InterruptedException {
        return createBug(summary, description);
    }

    public void transitionIssue(JiraIssue issue, String transitionName, String comment)
            throws IOException, InterruptedException {
        JSONObject transitions = get("/issue/" + encode(issue.key()) + "/transitions");
        String transitionId = null;
        for (Object entry : transitions.getJSONArray("transitions")) {
            JSONObject transition = (JSONObject) entry;
            if (transitionName.equalsIgnoreCase(transition.optString("name"))) {
                transitionId = transition.getString("id");
                break;
            }
        }
        if (transitionId == null) {
            throw new IllegalStateException("JIRA transition not available for " + issue.key() + ": " + transitionName);
        }
        post("/issue/" + encode(issue.key()) + "/transitions",
                new JSONObject().put("transition", new JSONObject().put("id", transitionId)));
        if (comment != null && !comment.isBlank()) {
            post("/issue/" + encode(issue.key()) + "/comment", new JSONObject().put("body", comment));
        }
    }

    public void linkIssues(String issueOne, String issueTwo, String linkName)
            throws IOException, InterruptedException {
        JSONObject body = new JSONObject()
                .put("type", new JSONObject().put("name", linkName))
                .put("inwardIssue", new JSONObject().put("key", issueOne))
                .put("outwardIssue", new JSONObject().put("key", issueTwo));
        post("/issueLink", body);
    }

    public void addIssueAttachment(JiraIssue issue, File file, String name)
            throws IOException, InterruptedException {
        String boundary = "----cukes-" + UUID.randomUUID();
        byte[] content = Files.readAllBytes(file.toPath());
        byte[] prefix = ("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + name + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[prefix.length + content.length + suffix.length];
        System.arraycopy(prefix, 0, body, 0, prefix.length);
        System.arraycopy(content, 0, body, prefix.length, content.length);
        System.arraycopy(suffix, 0, body, prefix.length + content.length, suffix.length);

        HttpRequest request = requestBuilder("/issue/" + encode(issue.key()) + "/attachments")
                .header("X-Atlassian-Token", "no-check")
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        send(request, 200, 201);
    }

    public Optional<JiraIssue> getExistingBug(String summary, String ignoredDescription)
            throws IOException, InterruptedException {
        String project = require(properties, "project");
        String escaped = summary.replace("\\", "\\\\").replace("\"", "\\\"");
        String jql = "project = " + project + " AND statusCategory != Done AND issuetype = Bug AND summary ~ \""
                + escaped + "\" ORDER BY created DESC";
        JSONObject result = get("/search?maxResults=1&fields=key&jql=" + encode(jql));
        JSONArray issues = result.optJSONArray("issues");
        return issues == null || issues.length() == 0
                ? Optional.empty()
                : Optional.of(new JiraIssue(issues.getJSONObject(0).getString("key")));
    }

    public JiraIssue createBug(String summary, String description) throws IOException, InterruptedException {
        String jiraPath = Objects.requireNonNullElse(Property.getProperty("jiraPath"),
                "/src/test/resources/config/jira/");
        File template = new File(System.getProperty("user.dir") + jiraPath + "bug.json");
        JSONObject payload;
        if (template.isFile()) {
            payload = JsonHelper.getJSONData(template.getPath());
            payload.getJSONObject("fields").put("summary", summary).put("description", description);
        } else {
            payload = defaultBugPayload(summary, description);
        }
        JSONObject response = post("/issue", payload);
        return new JiraIssue(response.getString("key"));
    }

    /**
     * Legacy JIRA-to-Gherkin export was tightly coupled to a custom issue schema.
     * Projects must provide their own exporter using {@link #search(String, int)}.
     */
    public void buildFeatureFiles() {
        throw new UnsupportedOperationException(
                "JIRA feature export requires a project-specific issue-field mapping. "
                        + "Use JIRAHelper.search(jql, maxResults) and implement a schema adapter.");
    }

    public JSONObject search(String jql, int maxResults) throws IOException, InterruptedException {
        return get("/search?maxResults=" + maxResults + "&jql=" + encode(jql));
    }

    private JSONObject defaultBugPayload(String summary, String description) {
        JSONObject fields = new JSONObject()
                .put("project", new JSONObject().put("key", require(properties, "project")))
                .put("issuetype", new JSONObject().put("name", properties.getString("bugType", "Bug")))
                .put("summary", summary)
                .put("description", description)
                .put("labels", new JSONArray().put("AutomatedTest").put("Cucumber"));

        Iterator<String> keys = properties.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("field.")) {
                fields.put(key.substring("field.".length()), resolveValue(properties.getString(key)));
            }
        }
        return new JSONObject().put("fields", fields);
    }

    private Object resolveValue(String value) {
        if (value != null && value.startsWith("<") && value.endsWith(">")) {
            return Objects.requireNonNullElse(Property.getVariable(value.substring(1, value.length() - 1)), "");
        }
        return value;
    }

    private JSONObject get(String path) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(path).GET().build();
        return json(send(request, 200));
    }

    private JSONObject post(String path, JSONObject payload) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        return json(send(request, 200, 201, 204));
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(baseUri.resolve("/rest/api/" + apiVersion + path))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", authorization);
    }

    private String send(HttpRequest request, int... expectedStatuses) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        for (int status : expectedStatuses) {
            if (response.statusCode() == status) {
                return response.body();
            }
        }
        throw new IOException("JIRA request failed: " + response.statusCode() + " " + response.body());
    }

    private static JSONObject json(String value) {
        return value == null || value.isBlank() ? new JSONObject() : new JSONObject(value);
    }

    private static String createAuthorization(FrameworkProperties properties) {
        String token = firstNonBlank(Property.getVariable("JIRA_TOKEN"), properties.getString("token"));
        String username = firstNonBlank(Property.getVariable("JIRA_USERNAME"), properties.getString("username"));
        String password = firstNonBlank(Property.getVariable("JIRA_PASSWORD"), properties.getString("password"));
        if (token != null && username == null) {
            return "Bearer " + token;
        }
        String secret = token != null ? token : password;
        if (username == null || secret == null) {
            throw new IllegalStateException("JIRA credentials are not configured. Use JIRA_USERNAME and JIRA_TOKEN.");
        }
        return "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String require(FrameworkProperties properties, String key) {
        String value = properties.getString(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required JIRA property is missing: " + key);
        }
        return value;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public record JiraIssue(String key) {
        public JiraIssue {
            Objects.requireNonNull(key, "key");
        }

        public String getKey() {
            return key;
        }
    }

    private static final class JiraOperationException extends RuntimeException {
        private JiraOperationException(Throwable cause) {
            super(cause);
        }
    }
}
