package utils;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.qameta.allure.Allure;

public final class ApiReportHelper {

    private static final String MASK = "********";
    private static final int MAX_CONSOLE_PAYLOAD_CHARS = 1000;
    private static final int MAX_REPORT_TEXT_PAYLOAD_CHARS = 3000;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "token",
            "access_token",
            "refresh_token",
            "authorization",
            "x-auth-token",
            "secret",
            "api-key",
            "apikey");

    private ApiReportHelper() {
    }

    public static void logTransaction(
            String stepName,
            HttpResponse<String> response,
            Map<String, String> requestBody) {
        if (response == null) {
            return;
        }

        HttpRequest request = response.request();
        long responseTime = ApiHelper.getResponseTimeMillis(response);
        String headline = request.method() + " " + request.uri()
                + " | HTTP " + response.statusCode()
                + " | " + formatDuration(responseTime);

        Allure.step(stepName + " | " + headline);
        Allure.addAttachment("API transaction", "text/plain", transactionSummary(request, response, responseTime), ".txt");

        if (!request.headers().map().isEmpty()) {
            Allure.addAttachment("Request headers", "text/plain", formatHeaders(request.headers()), ".txt");
        }

        if (requestBody != null && !requestBody.isEmpty()) {
            Allure.addAttachment("Request body", "application/json", formatMapAsJson(requestBody), ".json");
        }

        if (!response.headers().map().isEmpty()) {
            Allure.addAttachment("Response headers", "text/plain", formatHeaders(response.headers()), ".txt");
        }

        String body = response.body() == null ? "" : response.body().trim();
        if (!body.isEmpty()) {
            String safeBody = maskSensitiveJson(body);
            if (looksLikeJson(safeBody)) {
                Allure.addAttachment("Response body", "application/json", prettyJson(safeBody), ".json");
            } else {
                Allure.addAttachment("Response body", "text/plain",
                        truncate(safeBody, MAX_REPORT_TEXT_PAYLOAD_CHARS), ".txt");
            }
        }
    }

    public static void logAssertion(boolean passed, String message) {
        Allure.step(message + (passed ? " | passed" : " | failed"));
    }

    public static String sanitizePayload(String payload) {
        String safePayload = maskSensitiveJson(payload == null ? "" : payload).trim();
        return truncate(safePayload.replaceAll("\\s+", " "), MAX_CONSOLE_PAYLOAD_CHARS);
    }

        private static String transactionSummary(HttpRequest request, HttpResponse<String> response, long responseTime) {
        return "Method: " + request.method() + System.lineSeparator()
            + "URL: " + request.uri() + System.lineSeparator()
            + "HTTP Status: " + response.statusCode() + System.lineSeparator()
            + "Response Time: " + formatDuration(responseTime) + System.lineSeparator()
            + "Content Type: " + ApiHelper.getContentType(response);
    }

        private static String formatHeaders(HttpHeaders headers) {
        List<String> rows = new ArrayList<>();
        headers.map()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase(Locale.ROOT)))
            .forEach(entry -> rows.add(entry.getKey() + ": "
                + (isSensitiveKey(entry.getKey()) ? MASK : String.join(", ", entry.getValue()))));
        return String.join(System.lineSeparator(), rows);
    }

    private static String formatMapAsJson(Map<String, String> values) {
        List<Map.Entry<String, String>> entries = values.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, String> entry = entries.get(index);
            String value = isSensitiveKey(entry.getKey()) ? MASK : entry.getValue();
            json.append("  \"")
                    .append(escapeJson(entry.getKey()))
                    .append("\": \"")
                    .append(escapeJson(value))
                    .append("\"");
            if (index < entries.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("}");
        return json.toString();
    }

    private static String maskSensitiveJson(String body) {
        String masked = body;
        for (String key : SENSITIVE_KEYS) {
            String quotedKey = Pattern.quote(key);
            masked = masked.replaceAll("(?i)(\"" + quotedKey + "\"\\s*:\\s*\")([^\"]*)(\")", "$1" + MASK + "$3");
        }
        return masked;
    }

    private static boolean looksLikeJson(String value) {
        String trimmed = value == null ? "" : value.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private static String prettyJson(String json) {
        String trimmed = json == null ? "" : json.trim();
        if (!looksLikeJson(trimmed)) {
            return trimmed;
        }

        StringBuilder pretty = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escaping = false;
        for (int index = 0; index < trimmed.length(); index++) {
            char current = trimmed.charAt(index);

            if (escaping) {
                pretty.append(current);
                escaping = false;
                continue;
            }

            if (current == '\\' && inString) {
                pretty.append(current);
                escaping = true;
                continue;
            }

            if (current == '"') {
                pretty.append(current);
                inString = !inString;
                continue;
            }

            if (inString) {
                pretty.append(current);
                continue;
            }

            switch (current) {
                case '{':
                case '[':
                    pretty.append(current).append('\n');
                    indent++;
                    appendIndent(pretty, indent);
                    break;
                case '}':
                case ']':
                    pretty.append('\n');
                    indent = Math.max(0, indent - 1);
                    appendIndent(pretty, indent);
                    pretty.append(current);
                    break;
                case ',':
                    pretty.append(current).append('\n');
                    appendIndent(pretty, indent);
                    break;
                case ':':
                    pretty.append(": ");
                    break;
                default:
                    if (!Character.isWhitespace(current)) {
                        pretty.append(current);
                    }
                    break;
            }
        }
        return pretty.toString();
    }

    private static void appendIndent(StringBuilder builder, int indent) {
        builder.append("  ".repeat(Math.max(0, indent)));
    }

    private static String formatDuration(long millis) {
        return millis < 0 ? "not captured" : millis + " ms";
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "... [truncated, total chars: " + value.length() + "]";
    }
}
