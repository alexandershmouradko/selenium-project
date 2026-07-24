package uk.ndc.csa.utilities.restapi;

import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class RestData {

	private RequestSpecification request=null;
	private JSONObject requestJSON = null;
	private String requestString = null;
	
	private Response response = null;
	private ValidatableResponse respValidator = null;
	private JsonPath respJsonPath = null;
	private String respString = null;

	public RequestSpecification getRequest(){
		return request;
	}
	
	public Response getResponse(){
		return response;
	}
	
	public ValidatableResponse getRespValidator(){
		return respValidator;
	}
	
	public JsonPath getRespJsonPath(){
		return respJsonPath;
	}
	
	public String getRespString(){
		return respString;
	}
	
	public JSONObject getRequestJSON() {
		return requestJSON;
	}
	
	public String getRequestString() {
		return requestString;
	}

	public void setRequest(RequestSpecification request){
		this.request = request;
	}

	public void setResponse(Response response) {
		this.response = response;
		setRespValidator(response);
		setRespJsonPath(response);
		setRespString(response);
	}

	public void setRespValidator(Response response) {
		this.respValidator = response.then();
	}

	public void setRespJsonPath(Response response) {	
		this.respJsonPath = new JsonPath(response.asString());
	}

	public void setRespString(Response response) {
		this.respString = response.asString();
	}
	
	public void setRequestJSON(JSONObject requestJSON) {
		this.requestJSON = requestJSON;
	}
	
	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}
	
}
