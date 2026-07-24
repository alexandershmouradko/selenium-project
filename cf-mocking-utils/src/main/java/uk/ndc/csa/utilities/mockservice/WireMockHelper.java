package uk.ndc.csa.utilities.mockservice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** Thread-safe WireMock 3 lifecycle and registration facade. */
public final class WireMockHelper {
    private static final ThreadLocal<WireMockServer> SERVER = new ThreadLocal<>();

    private WireMockHelper() {
    }

    public static WireMockServer startWireMock(int port, String classpathPath) {
        closeCurrent();
        var configuration = wireMockConfig();
        if (port <= 0) {
            configuration.dynamicPort();
        } else {
            configuration.port(port);
        }
        if (classpathPath != null && !classpathPath.isBlank()) {
            configuration.usingFilesUnderClasspath(classpathPath);
        }
        WireMockServer server = new WireMockServer(configuration);
        server.start();
        SERVER.set(server);
        return server;
    }

    public static WireMockServer currentServer() {
        WireMockServer server = SERVER.get();
        if (server == null || !server.isRunning()) {
            throw new IllegalStateException("WireMock server is not running in the current test thread");
        }
        return server;
    }

    public static void resetWireMock(WireMock ignoredLegacyClient) {
        currentServer().resetAll();
    }

    public static void closeWireMock(WireMockServer server) {
        if (server != null) {
            server.stop();
        }
        SERVER.remove();
    }

    public static void closeCurrent() {
        WireMockServer server = SERVER.get();
        if (server != null) {
            server.stop();
            SERVER.remove();
        }
    }

    public static WireMock registerWireMockService(
            WireMock ignoredLegacyClient,
            String action,
            String path,
            String requestBody,
            int responseCode,
            String responseHeaderKey,
            String responseHeaderValue,
            String responseBody) {
        var mapping = request(action, urlEqualTo(path));
        if (requestBody != null && !requestBody.isBlank()) {
            mapping.withRequestBody(WireMock.equalToJson(requestBody, true, true));
        }
        var response = aResponse().withStatus(responseCode);
        if (responseHeaderKey != null && !responseHeaderKey.isBlank()) {
            response.withHeader(responseHeaderKey, responseHeaderValue);
        }
        if (responseBody != null) {
            response.withBody(responseBody);
        }
        currentServer().stubFor(mapping.willReturn(response));
        return ignoredLegacyClient;
    }
}
