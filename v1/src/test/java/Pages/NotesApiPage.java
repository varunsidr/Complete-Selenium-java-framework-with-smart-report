package Pages;

import org.testng.asserts.SoftAssert;
import utils.ApiHelper;
import utils.ApiReportHelper;

import java.net.http.HttpResponse;
import java.util.Map;

public class NotesApiPage {

    public void verifyHealthCheckResponse(HttpResponse<String> response) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("GET /health-check", response, Map.of());

        reportEquals(softAssert, response.statusCode(), 200, "Health-check API should return HTTP 200");
        reportTrue(softAssert, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":true"), "Response should mark success as true");
        reportTrue(softAssert, responseBody.contains("\"status\":200"), "Response should include API status 200");
        reportTrue(softAssert, responseBody.contains("\"message\":\"Notes API is Running\""),
                "Response should confirm that the Notes API is running");
        softAssert.assertAll();
    }

    public void verifyUnknownHealthCheckRouteResponse(
            HttpResponse<String> response,
            String endpoint) {
        String responseBody = response.body() == null ? "" : response.body();
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("GET " + endpoint + " - not found", response, Map.of());

        reportEquals(softAssert, response.statusCode(), 404, "Unknown health-check route should return HTTP 404");
        reportTrue(softAssert, !responseBody.contains("\"success\":true"),
                "Unknown route should not return a successful health-check payload");
        softAssert.assertAll();
    }

    public void verifyHttpRedirectsToHttps(
            HttpResponse<String> response,
            String stepName,
            String expectedHttpsUrl,
            Map<String, String> requestBody) {
        String locationHeader = response.headers().firstValue("location").orElse("");
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse(stepName, response, requestBody);

        reportTrue(softAssert, isRedirectStatus(response.statusCode()),
                "HTTP endpoint should return a redirect status | actual: " + response.statusCode());
        reportEquals(softAssert, locationHeader, expectedHttpsUrl,
                "HTTP endpoint should redirect to HTTPS Location header");
        softAssert.assertAll();
    }

    public void verifyInvalidLoginResponse(HttpResponse<String> response, Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - invalid credentials", response, requestBody);

        reportEquals(softAssert, response.statusCode(), 401, "Invalid login should return HTTP 401");
        reportTrue(softAssert, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":false"), "Response should mark success as false");
        reportTrue(softAssert, responseBody.contains("\"status\":401"), "Response should include API status 401");
        reportTrue(softAssert, responseBody.contains("\"message\":\"Incorrect email address or password\""),
                "Response should explain that credentials are incorrect");
        softAssert.assertAll();
    }

    public void verifySuccessfulLoginResponse(
            HttpResponse<String> response,
            String expectedEmail,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        String token = ApiHelper.extractJsonString(responseBody, "token");
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - valid credentials", response, requestBody);

        reportEquals(softAssert, response.statusCode(), 200, "Valid login should return HTTP 200");
        reportTrue(softAssert, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":true"), "Response should mark success as true");
        reportTrue(softAssert, responseBody.contains("\"status\":200"), "Response should include API status 200");
        reportTrue(softAssert, responseBody.contains("\"message\":\"Login successful\""),
                "Response should confirm login success");
        reportTrue(softAssert, responseBody.contains("\"email\":\"" + expectedEmail + "\""),
                "Response should include logged-in user's email");
        reportTrue(softAssert, !token.isEmpty(), "Response should include an auth token");
        softAssert.assertAll();
    }

    public void verifyBadRequestLoginResponse(
            HttpResponse<String> response,
            String expectedMessage,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/login - validation error", response, requestBody);

        reportEquals(softAssert, response.statusCode(), 400, "Bad login request should return HTTP 400");
        reportTrue(softAssert, contentType.contains("application/json"), "Response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":false"), "Response should mark success as false");
        reportTrue(softAssert, responseBody.contains("\"status\":400"), "Response should include API status 400");
        reportTrue(softAssert, responseBody.contains("\"message\":\"" + expectedMessage + "\""),
                "Response should include expected validation message: " + expectedMessage);
        softAssert.assertAll();
    }

        public void verifyUserRegistrationResponse(
            HttpResponse<String> response,
            String expectedEmail,
            Map<String, String> requestBody) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("POST /users/register - setup user", response, requestBody);

        reportEquals(softAssert, response.statusCode(), 201, "Temporary user registration should return HTTP 201");
        reportTrue(softAssert, contentType.contains("application/json"), "Registration response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":true"), "Registration response should mark success as true");
        reportTrue(softAssert, responseBody.contains("\"status\":201"), "Registration response should include API status 201");
        reportTrue(softAssert, responseBody.contains("\"message\":\"User account created successfully\""),
                "Registration response should confirm account creation");
        reportTrue(softAssert, responseBody.contains("\"email\":\"" + expectedEmail + "\""),
                "Registration response should include temporary user's email");
        softAssert.assertAll();
    }

    public void verifyAccountCleanupResponse(HttpResponse<String> response) {
        String responseBody = response.body();
        String contentType = ApiHelper.getContentType(response);
        SoftAssert softAssert = new SoftAssert();

        reportApiResponse("DELETE /users/delete-account - cleanup", response, Map.of());

        reportEquals(softAssert, response.statusCode(), 200, "Temporary account cleanup should return HTTP 200");
        reportTrue(softAssert, contentType.contains("application/json"), "Cleanup response content type should be JSON");
        reportTrue(softAssert, responseBody.contains("\"success\":true"), "Cleanup response should mark success as true");
        reportTrue(softAssert, responseBody.contains("\"status\":200"), "Cleanup response should include API status 200");
        reportTrue(softAssert, responseBody.contains("\"message\":\"Account successfully deleted\""),
                "Cleanup response should confirm account deletion");
        softAssert.assertAll();
    }

    private void reportApiResponse(
            String stepName,
            HttpResponse<String> response,
            Map<String, String> requestBody) {
        System.out.println("[" + stepName + "] Status Code: " + response.statusCode());
        System.out.println("[" + stepName + "] Response Body: " + ApiReportHelper.sanitizePayload(response.body()));
        ApiReportHelper.logTransaction(stepName, response, requestBody);
    }

    private void reportEquals(SoftAssert softAssert, int actual, int expected, String message) {
        boolean passed = actual == expected;
        reportAssertion(passed, message + " | expected: " + expected + ", actual: " + actual);
        softAssert.assertEquals(actual, expected, message);
    }

    private void reportEquals(SoftAssert softAssert, String actual, String expected, String message) {
        boolean passed = expected.equals(actual);
        reportAssertion(passed, message + " | expected: " + expected + ", actual: " + actual);
        softAssert.assertEquals(actual, expected, message);
    }

    private void reportTrue(SoftAssert softAssert, boolean condition, String message) {
        reportAssertion(condition, message);
        softAssert.assertTrue(condition, message);
    }

    private void reportAssertion(boolean passed, String message) {
        ApiReportHelper.logAssertion(passed, message);
    }

    private boolean isRedirectStatus(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 307 || statusCode == 308;
    }
}
