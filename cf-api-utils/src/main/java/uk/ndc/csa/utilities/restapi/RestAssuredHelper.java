package uk.ndc.csa.utilities.restapi;

import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import java.util.List;
import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.core.IsEqual;
import org.json.JSONObject;

/**
 * Helper class with wrapper methods to leverage the Rest Assured library.
 * Allows rest api calls to be configured and invoked followed by response validation.
 */
public class RestAssuredHelper {

    public static RequestSpecification setBasicAuth(RequestSpecification request, String uname, String pword) {
        return request.auth().preemptive().basic(uname, pword);
    }

    public static RequestSpecification setChallengedBasicAuth(RequestSpecification request, String uname, String pword) {
        return request.auth().basic(uname, pword);
    }

    public static RequestSpecification setBaseURI(RequestSpecification request, String uri) {
        return request.baseUri(uri);
    }

    public static RequestSpecification setBasePath(RequestSpecification request, String basepath) {
        return request.basePath(basepath);
    }

    public static RequestSpecification setPort(RequestSpecification request, int port) {
        return request.port(port);
    }

    public static RequestSpecification setHeader(RequestSpecification request, String key, String val) {
        return request.header(key, val);
    }

    public static RequestSpecification setParam(RequestSpecification request, String type, String key, String val) {
        switch (type) {
            case "parameters":
                request.param(key, val);
                break;
            case "form parameters":
                request.formParam(key, val);
                break;
            case "query parameters":
                request.queryParam(key, val);
                break;
            case "path parameters":
                request.pathParam(key, val);
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }

        return request;
    }

    public static RequestSpecification setParamList(RequestSpecification request, String key, List<String> val) {
        return request.param(key, val);
    }

    public static RequestSpecification setBody(RequestSpecification request, String content) {
        return request.body(content);
    }

    public static Response callAPI(RequestSpecification request, String method, String path) {
        if (request == null) {
            throw new IllegalArgumentException("Request specification must not be null");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("HTTP method must not be blank");
        }
        return switch (method.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "GET" -> request.get(path);
            case "PUT" -> request.put(path);
            case "POST" -> request.post(path);
            case "PATCH" -> request.patch(path);
            case "DELETE" -> request.delete(path);
            case "HEAD" -> request.head(path);
            case "OPTIONS" -> request.options(path);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    public static void checkStatus(RestData restData, int statusCode) {
        restData.getRespValidator().assertThat().statusCode(statusCode);
    }

    public static void checkStatus(RestData restData, String msg) {
        restData.getRespValidator().assertThat().statusLine(containsString(msg));
    }

    public static void checkSchema(RestData restData, String type, String path) {
        switch (type) {
            case "json":
                restData.getRespValidator().body(matchesJsonSchemaInClasspath(path));
                break;
            case "xml":
                restData.getRespValidator().body(matchesXsd(path));
                break;
        }

    }

    public static void checkResponseTime(RestData restData, long duration) {
        restData.getRespValidator().assertThat().time(lessThan(duration));
    }

    public static void checkHeader(RestData restData, String key, String matcher, Object val) {
        String act = restData.getResponse().header(key);

        switch (matcher) {
            case "equals":
                assertThat(act, equalTo(val));
                break;
            case "regex":
                assertThat("value " + act.toString() + " does not match regex " + val.toString(), act.toString().matches(val.toString()), is(true));
                break;
            case "isNull":
                assertThat(act, nullValue());
                break;
            case "!isNull":
                assertThat(act, not(nullValue()));
                break;
        }
    }

    public static void checkBody(JsonPath jsonPath, Object[] obj, String element, String matcher) {
        Object act = jsonPath.get(element);//from(respString).get(element);
        Object nullObj = null;
        List<Object> list = null;

        switch (matcher) {
            case "is":
                assertThat(act, is(obj[0]));
                break;
            case "equals":
                assertThat(act, equalTo(obj[0]));
                break;
            case "hasItem":
                list = jsonPath.get(element);
                assertThat(list, hasItem(obj[0]));
                break;
            case "hasItems":
                list = jsonPath.get(element);
                assertThat(list, hasItems(obj));
                break;
            case "contains":
                list = jsonPath.get(element);
                assertThat(list, contains(obj));
                break;
            case "containsAnyOrder":
                list = jsonPath.get(element);
                assertThat(list, containsInAnyOrder(obj));
                break;
            case "hasSize":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, hasSize((Integer) obj[0]));
                }else{
                    assertThat(new JSONObject(new Gson().toJson(act)).length(), IsEqual.equalTo(obj[0]));
                }
                break;
            case "isNull":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, contains(nullObj));
                } else {
                    assertThat(act, nullValue());
                }
                break;
            case "isEmpty":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, is(empty()));
                }else{
                    assertThat(act.toString().trim(), equalTo("{}"));
                }
                break;
            case "startsWith":
                assertThat(act.toString(), startsWith(obj[0].toString()));
                break;
            case "endsWith":
                assertThat(act.toString(), endsWith(obj[0].toString()));
                break;
            case "containsString":
                assertThat(act.toString(), containsString(obj[0].toString()));
                break;    //use when working on single element
            case "containsStringArray":
                assertThat(act.toString(), containsString(obj[0].toString()));
                break;    //use when working on string array
            case "regex":
                assertThat("value " + act.toString() + " does not match regex " + obj[0].toString(), act.toString().matches(obj[0].toString()), is(true));
                break;


            case "!is":
                assertThat(act, not(is(obj[0])));
                break;
            case "!equals":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(equalTo(obj)));
                } else {
                    assertThat(act, not(equalTo(obj[0])));
                }
                break;
            case "!hasItem":
                list = jsonPath.get(element);
                assertThat(list, not(hasItem(obj[0])));
                break;
            case "!hasItems":
                list = jsonPath.get(element);
                assertThat(list, not(hasItems(obj)));
                break;
            case "!contains":
                list = jsonPath.get(element);
                assertThat(list, not(contains(obj)));
                break;
            case "!containsAnyOrder":
                list = jsonPath.get(element);
                assertThat(list, not(containsInAnyOrder(obj)));
                break;
            case "!hasSize":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(hasSize((Integer) obj[0])));
                }else{
                    assertThat(new JSONObject(new Gson().toJson(act)).length(), not(IsEqual.equalTo(obj[0])));
                }
                break;
            case "!isNull":
                if (act instanceof List) {
                    list = jsonPath.get(element);
                    assertThat(list, not(contains(nullObj)));
                } else {
                    assertThat(act, not(nullValue()));
                }
                break;
            case "!isEmpty":
                if (act instanceof List) {
                list = jsonPath.get(element);
                assertThat(list, not(is(empty())));
                }else{
                    assertThat(act.toString().trim(), not(equalTo("{}")));
                }
                break;
            case "!startsWith":
                assertThat(act.toString(), not(startsWith(obj[0].toString())));
                break;
            case "!endsWith":
                assertThat(act.toString(), not(endsWith(obj[0].toString())));
                break;
            case "!containsString":
                assertThat(act.toString(), not(containsString(obj[0].toString())));
                break;    //use when working on single element
            case "!containsStringArray":
                assertThat(act.toString(), not(containsString(obj[0].toString())));
                break;    //use when working on string array
            case "!regex":
                assertThat("value " + act.toString() + " should not match regex " + obj[0].toString(), act.toString().matches(obj[0].toString()), is(false));
                break;
        }
    }


}
