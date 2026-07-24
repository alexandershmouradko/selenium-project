package uk.ndc.csa.utilities.restapi;

import com.sun.net.httpserver.HttpServer;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class RestAssuredHelperSmokeTest {
    private HttpServer server;

    @BeforeMethod
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void rejectsUnsupportedHttpMethod() {
        IllegalArgumentException error = expectThrows(
                IllegalArgumentException.class,
                () -> RestAssuredHelper.callAPI(given(), "TRACE", "/health"));
        assertEquals(error.getMessage(), "Unsupported HTTP method: TRACE");
    }

    @Test
    public void invokesLocalApiUsingFrameworkHelper() {
        RequestSpecification request = given();
        RestAssuredHelper.setBaseURI(request, "http://127.0.0.1");
        RestAssuredHelper.setPort(request, server.getAddress().getPort());

        Response response = RestAssuredHelper.callAPI(request, "GET", "/health");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("status"), "UP");
    }
}
