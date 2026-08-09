package Pages;

import com.aventstack.extentreports.ExtentTest;
import org.testng.asserts.SoftAssert;
import utils.ApiHelper;
import utils.ApiReportHelper;

import java.net.http.HttpResponse;
import java.util.Map;

public class NotesApiPage {

    public void verifyHealthCheckResponse(HttpResponse<String> response, ExtentTest test) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("GET /health-check", response, test, Map.of());

        reportEquals(softAssert, test, response.statusCode(), 200, "Health-check API should return HTTP 200");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":true"), "Response should mark success as true");
        reportTrue(softAssert, test, responseBody.contains("\"status\":200"), "Response should include API status 200");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"Notes API is Running\""),
                "Response should confirm that the Notes API is running");
        softAssert.assertAll();
    }

    public void verifyUnknownHealthCheckRouteResponse(
            HttpResponse<String> response,
            ExtentTest test,
            String endpoint) {
        String responseBody = response.body() == null ? "" : response.body();
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("GET " + endpoint + " - not found", response, test, Map.of());

        reportEquals(softAssert, test, response.statusCode(), 404, "Unknown health-check route should return HTTP 404");
        reportTrue(softAssert, test, !responseBody.contains("\"success\":true"),
                "Unknown route should not return a successful health-check payload");
        softAssert.assertAll();
    }

    public void verifyHttpRedirectsToHttps(
            HttpResponse<String> response,
            ExtentTest test,
            String stepName,
            String expectedHttpsUrl,
            Map<String, String> requestBody) {
        String locationHeader = response.headers().firstValue("location").orElse("");
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse(stepName, response, test, requestBody);

        reportTrue(softAssert, test, isRedirectStatus(response.statusCode()),
                "HTTP endpoint should return a redirect status | actual: " + response.statusCode());
        reportEquals(softAssert, test, locationHeader, expectedHttpsUrl,
                "HTTP endpoint should redirect to HTTPS Location header");
        softAssert.assertAll();
    }

    public void verifyInvalidLoginResponse(HttpResponse<String> response, ExtentTest test, Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - invalid credentials", response, test, requestBody);

        reportEquals(softAssert, test, response.statusCode(), 401, "Invalid login should return HTTP 401");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":false"), "Response should mark success as false");
        reportTrue(softAssert, test, responseBody.contains("\"status\":401"), "Response should include API status 401");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"Incorrect email address or password\""),
                "Response should explain that credentials are incorrect");
        softAssert.assertAll();
    }

    public void verifySuccessfulLoginResponse(
            HttpResponse<String> response,
            ExtentTest test,
            String expectedEmail,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        String token = ApiHelper.extractJsonString(responseBody, "token");
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - valid credentials", response, test, requestBody);

        reportEquals(softAssert, test, response.statusCode(), 200, "Valid login should return HTTP 200");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":true"), "Response should mark success as true");
        reportTrue(softAssert, test, responseBody.contains("\"status\":200"), "Response should include API status 200");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"Login successful\""),
                "Response should confirm login success");
        reportTrue(softAssert, test, responseBody.contains("\"email\":\"" + expectedEmail + "\""),
                "Response should include logged-in user's email");
        reportTrue(softAssert, test, !token.isEmpty(), "Response should include an auth token");
        softAssert.assertAll();
    }

    public void verifyBadRequestLoginResponse(
            HttpResponse<String> response,
            ExtentTest test,
            String expectedMessage,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - validation error", response, test, requestBody);

        reportEquals(softAssert, test, response.statusCode(), 400, "Bad login request should return HTTP 400");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":false"), "Response should mark success as false");
        reportTrue(softAssert, test, responseBody.contains("\"status\":400"), "Response should include API status 400");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"" + expectedMessage + "\""),
                "Response should include expected validation message: " + expectedMessage);
        softAssert.assertAll();
    }

    public void verifyUserRegistrationResponse(
            HttpResponse<String> response,
            ExtentTest test,
            String expectedEmail,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/register - setup user", response, test, requestBody);

        reportEquals(softAssert, test, response.statusCode(), 201, "Temporary user registration should return HTTP 201");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Registration response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":true"), "Registration response should mark success as true");
        reportTrue(softAssert, test, responseBody.contains("\"status\":201"), "Registration response should include API status 201");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"User account created successfully\""),
                "Registration response should confirm account creation");
        reportTrue(softAssert, test, responseBody.contains("\"email\":\"" + expectedEmail + "\""),
                "Registration response should include temporary user's email");
        softAssert.assertAll();
    }

    public void verifyAccountCleanupResponse(HttpResponse<String> response, ExtentTest test) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("DELETE /users/delete-account - cleanup", response, test, Map.of());

        reportEquals(softAssert, test, response.statusCode(), 200, "Temporary account cleanup should return HTTP 200");
        reportTrue(softAssert, test, contentType.contains("application/json"), "Cleanup response content type should be JSON");
        reportTrue(softAssert, test, responseBody.contains("\"success\":true"), "Cleanup response should mark success as true");
        reportTrue(softAssert, test, responseBody.contains("\"status\":200"), "Cleanup response should include API status 200");
        reportTrue(softAssert, test, responseBody.contains("\"message\":\"Account successfully deleted\""),
                "Cleanup response should confirm account deletion");
        softAssert.assertAll();
    }

    private void reportApiResponse(
            String stepName,
            HttpResponse<String> response,
            ExtentTest test,
            Map<String, String> requestBody) {
        System.out.println("[" + stepName + "] Status Code: " + response.statusCode());
        System.out.println("[" + stepName + "] Response Body: " + ApiReportHelper.sanitizePayload(response.body()));
        ApiReportHelper.logTransaction(test, stepName, response, requestBody);
    }

    private void reportEquals(SoftAssert softAssert, ExtentTest test, int actual, int expected, String message) {
        boolean passed = actual == expected;
        reportAssertion(test, passed, message + " | expected: " + expected + ", actual: " + actual);
        softAssert.assertEquals(actual, expected, message);
    }

    private void reportEquals(SoftAssert softAssert, ExtentTest test, String actual, String expected, String message) {
        boolean passed = expected.equals(actual);
        reportAssertion(test, passed, message + " | expected: " + expected + ", actual: " + actual);
        softAssert.assertEquals(actual, expected, message);
    }

    private void reportTrue(SoftAssert softAssert, ExtentTest test, boolean condition, String message) {
        reportAssertion(test, condition, message);
        softAssert.assertTrue(condition, message);
    }

    private void reportAssertion(ExtentTest test, boolean passed, String message) {
        ApiReportHelper.logAssertion(test, passed, message);
    }

    private boolean isRedirectStatus(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 307 || statusCode == 308;
    }
}
