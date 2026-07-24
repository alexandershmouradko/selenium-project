package uk.ndc.csa.utilities.restapi;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.filter.Filter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


import uk.ndc.csa.utilities.common.JsonHelper;
import uk.ndc.csa.utilities.common.ThreadContext;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;
import static uk.ndc.csa.utilities.common.JsonHelper.*;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * Common Steps class which invokes the RestAssuredHelper class and enables
 * rest api calls to be configured and invoked from cucumber features along
 * with detailed response validation.
 */

public class RestAssuredSteps {
    RestContext restContext;
    RequestSpecification request = given();

    public RestAssuredSteps(RestContext restContext) {
        this.restContext = restContext;
    }

    private String baseTestData = null;
    private String baseDataSet = null;
    private String api = null;
    private String path = null;
    private String method = null;

    public void resetRest() {
        request = given();
        restContext.getRestData().setRequest(request);
    }

    @Given("^a rest api \"(.*)\"$")
    public void setAPI(String api) {
        restContext.getRestData().setRequest(request);
        this.api = api;
        String envURI = ThreadContext.getInstance().getEnvironmentProps().getString(api);
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), envURI);
    }

    @Given("^(challenged basic|basic) authorisation$")
    public void setBasicAuth(String type) {
        String[] auth = ThreadContext.getInstance().getEnvironmentProps().getString(api + ".basicauth").split(":");
        if (type.equalsIgnoreCase("basic")) {
            RestAssuredHelper.setBasicAuth(restContext.getRestData().getRequest(), auth[0], auth[1]);
        } else {
            RestAssuredHelper.setChallengedBasicAuth(restContext.getRestData().getRequest(), auth[0], auth[1]);
        }
    }

    @Given("^a base uri \"(.*)\"$")
    public void setBaseURI(String uri) {
        RestAssuredHelper.setBaseURI(restContext.getRestData().getRequest(), uri);
    }

    @Given("^a base path \"(.*)\"$")
    public void setBasePath(String basepath) {
        RestAssuredHelper.setBasePath(restContext.getRestData().getRequest(), basepath);
    }

    @Given("^a port (\\d+)$")
    public void setPort(int port) {
        RestAssuredHelper.setPort(restContext.getRestData().getRequest(), port);
    }

    @Given("^a header$")
    public void setHeader(Map<String, String> map) {
        map.forEach((key, val) -> {
            RestAssuredHelper.setHeader(restContext.getRestData().getRequest(), key, val);
        });
    }

    @Given("^(form parameters|query parameters|path parameters|parameters)$")
    public void withParams(String type, Map<String, String> map) {
        map.forEach((key, val) -> {
            Boolean list = false;
            List<String> vals = new ArrayList<String>();
            if (val.contains("::")) {
                list = true;
                vals = Arrays.asList(val.split("::"));
            }

            if (!list)
                RestAssuredHelper.setParam(restContext.getRestData().getRequest(), type, key, val);
            else
                RestAssuredHelper.setParamList(restContext.getRestData().getRequest(), key, vals);
        });
    }

    @Given("^base input data \"([^\"]*)\"$")
    public void setBaseInputData(String arg1) throws Throwable {
        String[] str = arg1.split("\\.");
        baseTestData = "src/test/resources/testdata/inputs/" + str[0] + ".json";
        baseDataSet = str[1];
    }

    @When("^a request body$")
    @Given("^a request body \"(.*)\"$")
    public void requestBody(String data) throws IOException {
        String jsonData = createJson(data);
        RestAssuredHelper.setBody(restContext.getRestData().getRequest(), jsonData);
    }

    @When("^the system requests (GET|PUT|POST|PATCH|DELETE) \"(.*)\"$")
    public void apiGetRequest(String apiMethod, String path) {
        this.path = path;
        this.method = apiMethod;
        Response response = RestAssuredHelper.callAPI(restContext.getRestData().getRequest(), apiMethod, path);
        restContext.getRestData().setResponse(response);
        resetRest();        //enables multiple api calls within single scenario
    }

    @Then("^the response code is (\\d+)$")
    public void verify_status_code(int code) throws NumberFormatException, IOException {
        RestAssuredHelper.checkStatus(restContext.getRestData(), code);
        ThreadContext.getInstance().sa().assertAll();
    }

    @Then("^the response status is \"(.*)\"$")
    public void verify_status_message(String msg) throws NumberFormatException, IOException {
        RestAssuredHelper.checkStatus(restContext.getRestData(), msg);
        ThreadContext.getInstance().sa().assertAll();
    }

    @Then("^the response time is less than (\\d+) milliseconds$")
    public void verifyResponseTime(long duration) {
        RestAssuredHelper.checkResponseTime(restContext.getRestData(), duration);
    }

    @Then("^the response header contains$")
    public void verifyHeader(List<List<String>> list) {
        Object val;
        String key;
        String matcher;
        for (List<String> row : list) {
            if (row.size() == 2) {
                matcher = "equals";
                key = row.get(0);
                val = row.get(1);
            } else {
                matcher = row.get(1);
                key = row.get(0);
                val = row.get(2);
            }
            RestAssuredHelper.checkHeader(restContext.getRestData(), key, matcher, val);
        }
    }

    @And("^the response body is empty$")
    public void responseBodyEmpty() throws IOException {
        if (!(restContext.getRestData().getRespString().equalsIgnoreCase("{}")
                || restContext.getRestData().getRespString().equalsIgnoreCase("[]"))) {
            fail("response body not empty....contains: " + restContext.getRestData().getRespString());
        }
    }

    @And("^the (json|response) body( strictly|) contains$")
    public void responseBodyValid(String type, String mode, DataTable table) throws IOException {
        List<List<String>> temp = table.cells();

        JSONCompareMode compareMode = (mode.trim().equals("strictly") ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT);

        Map<String, String> map;
        if (temp.get(0).size() == 1) {
            String[] filename = temp.get(1).get(0).replace("<<", "").replace(">>", "").split("\\.");
            JSONObject obj = getJSONData("src/test/resources/testdata/outputs/" + filename[0] + ".json");
            //JSONArray or JSONObject casting to preserve data ordering
            if (restContext.getRestData().getRespString().startsWith("[")) {
                JSONArray responseArray = null;
                if (type.equalsIgnoreCase("response")) {
                    responseArray = new JSONArray(restContext.getRestData().getRespString());
                } else {
                    responseArray = new JSONArray(ThreadContext.getInstance().testdataGet("jsonBody").toString());
                }
                //assertEquals(responseArray.toString(), obj.get(filename[1]).toString(),"Expected response body differs from actual");
                JSONAssert.assertEquals("Expected response body differs from actual", (JSONArray) obj.get(filename[1]), responseArray, compareMode);
            } else {
                JSONObject responseJsonObject = null;
                if (type.equalsIgnoreCase("response")) {
                    responseJsonObject = new JSONObject(restContext.getRestData().getRespString());
                } else {
                    responseJsonObject = new JSONObject(ThreadContext.getInstance().testdataGet("jsonBody").toString());
                }

                //assertEquals(responseJsonObject.toString(), obj.get(filename[1]).toString(),"Expected response body differs from actual");
                JSONAssert.assertEquals("Expected response body differs from actual", (JSONObject) obj.get(filename[1]), responseJsonObject, compareMode);
            }
        } else if (temp.get(0).size() == 2) {
            map = table.asMap(String.class, String.class);
            JSONObject obj = getJSONData(System.getProperty("user.dir") + ThreadContext.getInstance().getPathsProps().getString("apiStructurePath") + this.api + ".json");
            JSONObject json = obj.has(method + " " + path) ? obj.getJSONObject(method + " " + path)
                    : obj.has(method) ? obj.getJSONObject(method)
                    : obj.has(path) ? obj.getJSONObject(path)
                    : obj;

            List<ResponseValidator> list = new ArrayList<ResponseValidator>();
            map.forEach((k, v) -> {
                ResponseValidator rv = new ResponseValidator();
                rv.element = ((JSONObject) json.get(k)).getString("element");
                rv.matcher = ((JSONObject) json.get(k)).getString("matcher");
                rv.type = ((JSONObject) json.get(k)).getString("type");
                rv.value = v;
                list.add(rv);
            });
            responseContains(type, list);
        } else {
            responseContains(type, table.asList(ResponseValidator.class));
        }
    }


    @And("^the response matches the (json|xml) schema \"(.*)\"$")
    public void matchJSONSchema(String type, String path) {
        RestAssuredHelper.checkSchema(restContext.getRestData(), type, "testdata/schemas/" + path);
    }


    @And("^trace out request response$")
    public void traceOut() {
        System.out.println("request: " + restContext.getRestData().getRequestString());
        System.out.println("response: " + restContext.getRestData().getRespString());
    }

    public void responseContains(String type, List<ResponseValidator> table) {

        table.forEach((data) -> {
            ArrayList<Object> exp = new ArrayList<Object>();
            String[] str = null;
            if (!data.matcher.equalsIgnoreCase("regex") && data.value.length() > 1 && data.value.substring(0, 1).equalsIgnoreCase("[")) {
                str = data.value.substring(1, data.value.length() - 1).split(",");
            } else {
                str = new String[1];
                str[0] = data.value;
            }

            for (String s : str) {
                if (data.type.equals("int")) {
                    exp.add(Integer.parseInt(s));
                } else if (data.type.equals("num")) {
                    exp.add(Float.parseFloat(s));
                } else if (data.type.equals("boolean")) {
                    exp.add(Boolean.parseBoolean(s));
                } else {
                    exp.add(s);
                }
            }


            Object[] obj = exp.toArray(new Object[exp.size()]);
            try {
                JsonPath jsonPath = null;
                if (type.equalsIgnoreCase("response")) {
                    jsonPath = restContext.getRestData().getRespJsonPath();
                } else {
                    jsonPath = new JsonPath(ThreadContext.getInstance().testdataGet("jsonBody").toString());
                }

                RestAssuredHelper.checkBody(jsonPath, obj, data.element, data.matcher);
            } catch (AssertionError e) {
                ThreadContext.getInstance().sa().assertNull(e.getMessage());
            }
        });
        ThreadContext.getInstance().sa().assertAll();

    }

    public String createJson(String data) throws FileNotFoundException {
        String jsonData = null;
        JSONObject jsonTest = null;
        JSONObject jsonBase = null;
        FileReader reader;
        JSONTokener token;

        if (data.substring(0, 2).equalsIgnoreCase("<<")) {
            String[] str = data.replace("<<", "").replace(">>", "").split("\\.");
            String testdatafile = str[0].substring(0, str[0].length());
            String testdataset = str[1].substring(0, str[1].length());
            String path = "src/test/resources/testdata/inputs/" + testdatafile + ".json";
            reader = new FileReader(path);
            token = new JSONTokener(reader);
            jsonTest = (JSONObject) new JSONObject(token).get(testdataset);
        } else {
            jsonTest = new JSONObject(data);
        }


        if (baseTestData != null) {
            reader = new FileReader(baseTestData);
            token = new JSONTokener(reader);
            jsonBase = new JSONObject(token).getJSONObject(baseDataSet);
            jsonTest = JsonHelper.jsonMerge(jsonTest, jsonBase);
            jsonData = jsonTest.toString();
        }

        jsonData = jsonTest.toString();

        restContext.getRestData().setRequestJSON(jsonTest);
        restContext.getRestData().setRequestString(jsonData);
        return jsonData;
    }

    @Given("^the (request|response|request and response) (?:satisfy|satisfies) the contract \"([^\"]*)\"$")
    public void requestResponseSatisfiesContract(String mode, String contract) {
        Filter validationFilter = new SwaggerSoftValidationFilter(mode, contract);
        request.filter(validationFilter);
    }

    @Given("^the (request|response|request and response) (?:satisfy|satisfies) the contract$")
    public void requestResponseSatisfiesContract(String mode) {
        String contract = ThreadContext.getInstance().getEnvironmentProps()
                .getString(String.join(".", api, "Contract"));
        requestResponseSatisfiesContract(mode, contract);
    }

}



