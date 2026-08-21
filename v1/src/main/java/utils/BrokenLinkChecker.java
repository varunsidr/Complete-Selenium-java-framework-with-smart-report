package utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public final class BrokenLinkChecker {
    private BrokenLinkChecker() {
    }

    public static Reporter.BrokenLinkResult check(
            String href, URI baseUri, int connectTimeoutMillis, int readTimeoutMillis) {
        try {
            URI linkUri = URI.create(href);
            if (!linkUri.isAbsolute()) {
                linkUri = baseUri.resolve(linkUri);
            }

            URL url = linkUri.toURL();
            if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
                return new Reporter.BrokenLinkResult(
                        url.toString(), "SKIPPED", true, "Only HTTP and HTTPS links are checked");
            }

            HttpResponse response = request(url, "HEAD", connectTimeoutMillis, readTimeoutMillis);
            if (response.statusCode == HttpURLConnection.HTTP_BAD_METHOD
                    || response.statusCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
                response = request(url, "GET", connectTimeoutMillis, readTimeoutMillis);
            }

            String status = "HTTP " + response.statusCode
                    + ("GET".equals(response.requestMethod) ? " (GET fallback)" : "");
            String details = response.statusCode >= 400
                    ? "Link responded with an error code"
                    : "Link responded successfully";
            return new Reporter.BrokenLinkResult(response.finalUrl, status, response.statusCode < 400, details);
        } catch (IOException | IllegalArgumentException e) {
            return new Reporter.BrokenLinkResult(href, "ERROR", false, e.getMessage());
        }
    }

    private static HttpResponse request(
            URL url, String requestMethod, int connectTimeoutMillis, int readTimeoutMillis) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod(requestMethod);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(connectTimeoutMillis);
            connection.setReadTimeout(readTimeoutMillis);
            int statusCode = connection.getResponseCode();
            return new HttpResponse(statusCode, connection.getURL().toString(), requestMethod);
        } finally {
            connection.disconnect();
        }
    }

    private static class HttpResponse {
        private final int statusCode;
        private final String finalUrl;
        private final String requestMethod;

        private HttpResponse(int statusCode, String finalUrl, String requestMethod) {
            this.statusCode = statusCode;
            this.finalUrl = finalUrl;
            this.requestMethod = requestMethod;
        }
    }
}