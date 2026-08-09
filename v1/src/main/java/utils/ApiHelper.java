package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

public class ApiHelper {

    private static final String NOTES_API_BASE_URL = "https://practice.expandtesting.com/notes/api";
    private static final String NOTES_API_HTTP_BASE_URL = "http://practice.expandtesting.com/notes/api";
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 15;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final long RETRY_DELAY_MILLIS = 500;

    private static final Map<HttpResponse<String>, Long> RESPONSE_TIMES =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();

    private static final HttpClient NO_REDIRECT_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();

    public static HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .GET()
                .build();

        return send(request);
    }

    public static HttpResponse<String> getWithoutRedirect(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .GET()
                .build();

        return send(request, NO_REDIRECT_CLIENT);
    }

    public static HttpResponse<String> postForm(String endpoint, Map<String, String> formData)
            throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodeFormData(formData)))
                .build();

        return send(request);
    }

    public static HttpResponse<String> postFormWithoutRedirect(String endpoint, Map<String, String> formData)
            throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodeFormData(formData)))
                .build();

        return send(request, NO_REDIRECT_CLIENT);
    }

    public static HttpResponse<String> deleteWithToken(String endpoint, String token)
            throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .header("x-auth-token", token)
                .DELETE()
                .build();

        return send(request);
    }

    public static HttpResponse<String> deleteWithTokenWithoutRedirect(String endpoint, String token)
            throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(endpoint)
                .header("x-auth-token", token)
                .DELETE()
                .build();

        return send(request, NO_REDIRECT_CLIENT);
    }

    public static String getContentType(HttpResponse<String> response) {
        return response.headers()
                .firstValue("content-type")
                .orElse("")
                .toLowerCase(Locale.ROOT);
    }

    public static long getResponseTimeMillis(HttpResponse<String> response) {
        return RESPONSE_TIMES.getOrDefault(response, -1L);
    }

    public static String getNotesApiBaseUrl() {
        return NOTES_API_BASE_URL;
    }

    public static String getNotesApiHttpBaseUrl() {
        return NOTES_API_HTTP_BASE_URL;
    }

    public static String generateUniqueEmail(String prefix) {
        return prefix + "." + System.currentTimeMillis() + "@example.com";
    }

    public static String extractJsonString(String responseBody, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(responseBody == null ? "" : responseBody);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return send(request, CLIENT);
    }

    private static HttpResponse<String> send(HttpRequest request, HttpClient client) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                RESPONSE_TIMES.put(response, elapsedMillis);
                return response;
            } catch (IOException e) {
                lastException = e;
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    break;
                }
                Thread.sleep(RETRY_DELAY_MILLIS);
            }
        }

        throw lastException;
    }

    private static HttpRequest.Builder requestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(endpoint)))
                .header("Accept", "application/json")
                .header("User-Agent", "Hybrid-Framework-API-Tests/1.0")
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));
    }

    private static String buildUrl(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        if (endpoint.startsWith("/")) {
            return NOTES_API_BASE_URL + endpoint;
        }
        return NOTES_API_BASE_URL + "/" + endpoint;
    }

    private static String encodeFormData(Map<String, String> formData) {
        return formData.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
