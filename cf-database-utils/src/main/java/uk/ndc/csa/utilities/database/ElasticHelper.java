package uk.ndc.csa.utilities.database;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.ThreadContext;

/**
 * Version-neutral Elasticsearch REST facade. It avoids the removed legacy
 * RestHighLevelClient and works with supported Elasticsearch HTTP APIs.
 */
public final class ElasticHelper implements AutoCloseable {
    private final HttpClient client;
    private final URI endpoint;

    public ElasticHelper() {
        this(configurationEndpoint());
    }

    public ElasticHelper(String hostname, int port) {
        this(URI.create("http://" + hostname + ":" + port));
    }

    public ElasticHelper(URI endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public ElasticResponse search(String index, String jsonQuery) throws IOException, InterruptedException {
        return request("POST", "/" + encode(index) + "/_search", jsonQuery);
    }

    public ElasticResponse getDocument(String index, String ignoredLegacyType, String id)
            throws IOException, InterruptedException {
        return request("GET", "/" + encode(index) + "/_doc/" + encode(id), null);
    }

    public ElasticResponse updateDocument(String index, String ignoredLegacyType, String id, String json)
            throws IOException, InterruptedException {
        return request("POST", "/" + encode(index) + "/_update/" + encode(id), json);
    }

    public ElasticResponse insertDocument(String index, String ignoredLegacyType, String json)
            throws IOException, InterruptedException {
        return request("POST", "/" + encode(index) + "/_doc", json);
    }

    /** Legacy misspelling retained for compatibility. */
    public ElasticResponse insertDocumenet(String index, String type, String json)
            throws IOException, InterruptedException {
        return insertDocument(index, type, json);
    }

    public ElasticResponse insertDocument(String index, String ignoredLegacyType, String id, String json)
            throws IOException, InterruptedException {
        return request("PUT", "/" + encode(index) + "/_doc/" + encode(id), json);
    }

    /** Legacy misspelling retained for compatibility. */
    public ElasticResponse insertDocumenet(String index, String type, String id, String json)
            throws IOException, InterruptedException {
        return insertDocument(index, type, id, json);
    }

    public ElasticResponse deleteDocument(String index, String ignoredLegacyType, String id)
            throws IOException, InterruptedException {
        return request("DELETE", "/" + encode(index) + "/_doc/" + encode(id), null);
    }

    public ElasticResponse createIndex(String index) throws IOException, InterruptedException {
        return request("PUT", "/" + encode(index), null);
    }

    public ElasticResponse createIndex(String index, String settingsJson)
            throws IOException, InterruptedException {
        return request("PUT", "/" + encode(index), settingsJson);
    }

    public ElasticResponse deleteIndex(String index) throws IOException, InterruptedException {
        return request("DELETE", "/" + encode(index), null);
    }

    public ElasticResponse rawRequest(String method, String path, String body)
            throws IOException, InterruptedException {
        return request(method, path, body);
    }

    @Override
    public void close() {
        // java.net.http.HttpClient does not require explicit shutdown on Java 17.
    }

    private ElasticResponse request(String method, String path, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint.resolve(path))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new ElasticResponse(response.statusCode(), response.body());
    }

    private static URI configurationEndpoint() {
        FrameworkProperties props = ThreadContext.getInstance().getEnvironmentProps();
        String url = props.getString("ESurl", "http://localhost");
        if (url.matches("https?://[^:]+")) {
            return URI.create(url + ":" + props.getInt("ESport", 9200));
        }
        return URI.create(url);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public record ElasticResponse(int statusCode, String body) {
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
