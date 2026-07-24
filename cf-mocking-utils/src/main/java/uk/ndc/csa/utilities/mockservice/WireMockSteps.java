package uk.ndc.csa.utilities.mockservice;

import io.cucumber.java.en.Given;
import uk.ndc.csa.utilities.common.Property;

import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;


/** 
 * Helper class with methods to spin up a local MockServer and to register mockservices.
 * 
 * The request/response mock services can be defined via json config files
 * which will be automatically loaded when the mock server is started.  This format
 * enables more complex mocking to be defined including request content matching.
 * 
 * Starting and stopping the mock server is invoked from common cucumber steps.
 * 
 * For v.simple mock services based purely on the http method and path then these
 * can also be defined by calling the registerWireMockService() method which is itself
 * invoked from a common cucumber step (accepting a datatable input to define the service)
 */
public class WireMockSteps {
	
	
	//WireMock
	private WireMockServer server;
	private WireMock wireMock;
	
	@Given("^the wiremock server is started$")
	public void startWireMock(List<String> mock) throws Throwable {	
		
		server = WireMockHelper.startWireMock(Integer.parseInt(mock.get(1)), Property.getProperty("mockServicePath"));
		wireMock = new WireMock("localhost", server.port());
	}
	
	@Given("^reset the wiremockservice$")
	public void resetWireMock(){	
		WireMockHelper.resetWireMock(wireMock);
	}
	
	@Given("^close the wiremockservice$")
	public void closeWireMock(){
		WireMockHelper.closeWireMock(server);
		server = null;
		wireMock = null;
	}
	
	@Given("^a wiremock service is registered$")
	public void registerWireMockAPI(Map<String, String> param) throws Throwable {
		String action = param.get("action");
			String path = param.get("path");
			String requestBody = param.get("request body");
			int resCode = Integer.parseInt(param.get("response code"));
			String resHeaderKey = param.get("response header").contains(":")?param.get("response header").split(":")[0]:null;
			String resHeaderVal = param.get("response header").contains(":")?param.get("response header").split(":")[1]:null;
			String resBody = param.get("response body");
						
			WireMockHelper.registerWireMockService(wireMock, action, path, requestBody, resCode, resHeaderKey, resHeaderVal, resBody);		
	}	
}



