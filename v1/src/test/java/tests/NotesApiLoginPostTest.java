package tests;

import Pages.NotesApiPage;
import base.ApiBaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.ApiHelper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public class NotesApiLoginPostTest extends ApiBaseTest {

    private static final String LOGIN_ENDPOINT = "/users/login";
    private static final String REGISTER_ENDPOINT = "/users/register";
    private static final String DELETE_ACCOUNT_ENDPOINT = "/users/delete-account";
    private static final String PASSWORD = "Password123!";
    NotesApiPage notesApiPage = new NotesApiPage();

    @Test(description = "Verify Notes API login succeeds with valid credentials", groups = {"smoke", "api"})
    public void verifyLoginWithValidCredentials() throws IOException, InterruptedException {
        String email = ApiHelper.generateUniqueEmail("notes.login");
        String token = "";

        try {
            Map<String, String> registrationPayload = Map.of(
                    "name", "Codex Login User",
                    "email", email,
                    "password", PASSWORD);
            HttpResponse<String> registerResponse = ApiHelper.postForm(REGISTER_ENDPOINT, registrationPayload);
            notesApiPage.verifyUserRegistrationResponse(registerResponse, getTest(), email, registrationPayload);

            Map<String, String> loginPayload = Map.of(
                    "email", email,
                    "password", PASSWORD);
            HttpResponse<String> loginResponse = ApiHelper.postForm(LOGIN_ENDPOINT, loginPayload);
            token = ApiHelper.extractJsonString(loginResponse.body(), "token");
            notesApiPage.verifySuccessfulLoginResponse(loginResponse, getTest(), email, loginPayload);
        } finally {
            if (!token.isEmpty()) {
                HttpResponse<String> cleanupResponse = ApiHelper.deleteWithToken(DELETE_ACCOUNT_ENDPOINT, token);
                notesApiPage.verifyAccountCleanupResponse(cleanupResponse, getTest());
            }
        }
    }

    @Test(description = "Verify Notes API login rejects invalid credentials", groups = {"smoke", "api"})
    public void verifyLoginWithInvalidCredentials() throws IOException, InterruptedException {
        Map<String, String> loginPayload = Map.of(
                "email", "invalid.codex@example.com",
                "password", "WrongPassword123");
        HttpResponse<String> response = ApiHelper.postForm(LOGIN_ENDPOINT, loginPayload);
        notesApiPage.verifyInvalidLoginResponse(response, getTest(), loginPayload);
    }

    @Test(description = "Verify Notes API HTTP login redirects to HTTPS before processing credentials",
            groups = {"smoke", "api", "protocol", "security"})
    public void verifyHttpLoginRedirectsToHttps() throws IOException, InterruptedException {
        String httpUrl = ApiHelper.getNotesApiHttpBaseUrl() + LOGIN_ENDPOINT;
        String httpsUrl = ApiHelper.getNotesApiBaseUrl() + LOGIN_ENDPOINT;
        Map<String, String> loginPayload = Map.of(
                "email", "invalid.codex@example.com",
                "password", "WrongPassword123");

        HttpResponse<String> response = ApiHelper.postFormWithoutRedirect(httpUrl, loginPayload);
        notesApiPage.verifyHttpRedirectsToHttps(
                response,
                getTest(),
                "POST HTTP /users/login - redirect to HTTPS",
                httpsUrl,
                loginPayload);
    }

    @Test(description = "Verify Notes API HTTP delete-account redirects to HTTPS before deleting user",
            groups = {"smoke", "api", "protocol", "security"})
    public void verifyHttpDeleteAccountRedirectsToHttps() throws IOException, InterruptedException {
        String httpUrl = ApiHelper.getNotesApiHttpBaseUrl() + DELETE_ACCOUNT_ENDPOINT;
        String httpsUrl = ApiHelper.getNotesApiBaseUrl() + DELETE_ACCOUNT_ENDPOINT;

        HttpResponse<String> response = ApiHelper.deleteWithTokenWithoutRedirect(httpUrl, "dummy-token");
        notesApiPage.verifyHttpRedirectsToHttps(
                response,
                getTest(),
                "DELETE HTTP /users/delete-account - redirect to HTTPS",
                httpsUrl,
                Map.of());
    }

    @Test(description = "Verify Notes API login validates bad request payloads",
            groups = {"smoke", "api"},
            dataProvider = "badLoginPayloads")
    public void verifyLoginBadRequestResponses(String scenarioName, Map<String, String> requestBody, String expectedMessage)
            throws IOException, InterruptedException {
        HttpResponse<String> response = ApiHelper.postForm(LOGIN_ENDPOINT, requestBody);
        notesApiPage.verifyBadRequestLoginResponse(response, getTest(), expectedMessage, requestBody);
    }

    @DataProvider(name = "badLoginPayloads")
    public Object[][] badLoginPayloads() {
        return new Object[][] {
                {
                        "Invalid email format",
                        Map.of(
                                "email", "not-an-email",
                                "password", "WrongPassword123"),
                        "A valid email address is required"
                }
        };
    }
}
