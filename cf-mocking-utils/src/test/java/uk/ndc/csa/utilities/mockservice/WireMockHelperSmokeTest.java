package uk.ndc.csa.utilities.mockservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class WireMockHelperSmokeTest {

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        WireMockHelper.closeCurrent();
    }

    @Test
    public void startsRegistersServesAndStopsOnDynamicPort() throws Exception {
        WireMockServer server = WireMockHelper.startWireMock(0, null);
        WireMockHelper.registerWireMockService(
                null, "GET", "/health", null, 200,
                "Content-Type", "application/json", "{\"status\":\"UP\"}");

        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + server.port() + "/health"))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("UP"));

        WireMockHelper.closeWireMock(server);
        assertTrue(!server.isRunning());
    }
}
