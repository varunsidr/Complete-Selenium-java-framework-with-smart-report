package tests;

import Pages.NotesApiPage;
import base.ApiBaseTest;
import org.testng.annotations.Test;
import utils.ApiHelper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public class NotesApiHealthCheckTest extends ApiBaseTest {

    private static final String HEALTH_CHECK_ENDPOINT = "/health-check";
    private static final String UNKNOWN_HEALTH_CHECK_ENDPOINT = "/health-check/invalid";
    NotesApiPage notesApiPage = new NotesApiPage();

    @Test(description = "Verify Notes API health-check endpoint returns a successful response", groups = {"smoke", "api"})
    public void verifyNotesApiHealthCheck() throws IOException, InterruptedException {
        HttpResponse<String> response = ApiHelper.get(HEALTH_CHECK_ENDPOINT);
        notesApiPage.verifyHealthCheckResponse(response);
    }

    @Test(description = "Verify Notes API health-check unknown route returns not found", groups = {"smoke", "api", "negative"})
    public void verifyNotesApiUnknownHealthCheckRoute() throws IOException, InterruptedException {
        HttpResponse<String> response = ApiHelper.get(UNKNOWN_HEALTH_CHECK_ENDPOINT);
        notesApiPage.verifyUnknownHealthCheckRouteResponse(response, UNKNOWN_HEALTH_CHECK_ENDPOINT);
    }

    @Test(description = "Verify Notes API HTTP health-check redirects to HTTPS", groups = {"smoke", "api", "protocol", "security"})
    public void verifyNotesApiHttpHealthCheckRedirectsToHttps() throws IOException, InterruptedException {
        String httpUrl = ApiHelper.getNotesApiHttpBaseUrl() + HEALTH_CHECK_ENDPOINT;
        String httpsUrl = ApiHelper.getNotesApiBaseUrl() + HEALTH_CHECK_ENDPOINT;
        HttpResponse<String> response = ApiHelper.getWithoutRedirect(httpUrl);
        notesApiPage.verifyHttpRedirectsToHttps(
                response,
                "GET HTTP /health-check - redirect to HTTPS",
                httpsUrl,
                Map.of());
    }
}
