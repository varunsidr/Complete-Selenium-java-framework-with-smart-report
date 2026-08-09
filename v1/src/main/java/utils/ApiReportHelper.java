package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

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
            ExtentTest test,
            String stepName,
            HttpResponse<String> response,
            Map<String, String> requestBody) {
        if (test == null || response == null) {
            return;
        }

        HttpRequest request = response.request();
        long responseTime = ApiHelper.getResponseTimeMillis(response);
        ExtentTest node = test.createNode(stepName);
        String headline = request.method() + " " + request.uri()
                + " | HTTP " + response.statusCode()
                + " | " + formatDuration(responseTime);

        node.info(MarkupHelper.createLabel(headline, statusColor(response.statusCode())));
        node.info(MarkupHelper.createTable(summaryRows(request, response, responseTime)));

        if (!request.headers().map().isEmpty()) {
            node.info("Request Headers");
            node.info(MarkupHelper.createTable(headerRows(request.headers())));
        }

        if (requestBody != null && !requestBody.isEmpty()) {
            node.info("Request Body");
            node.info(MarkupHelper.createCodeBlock(formatMapAsJson(requestBody), CodeLanguage.JSON));
        }

        if (!response.headers().map().isEmpty()) {
            node.info("Response Headers");
            node.info(MarkupHelper.createTable(headerRows(response.headers())));
        }

        String body = response.body() == null ? "" : response.body().trim();
        if (!body.isEmpty()) {
            node.info("Response Body");
            String safeBody = maskSensitiveJson(body);
            if (looksLikeJson(safeBody)) {
                node.info(MarkupHelper.createCodeBlock(prettyJson(safeBody), CodeLanguage.JSON));
            } else {
                node.info("<pre>" + escapeHtml(truncate(safeBody, MAX_REPORT_TEXT_PAYLOAD_CHARS)) + "</pre>");
            }
        }
    }

    public static void logAssertion(ExtentTest test, boolean passed, String message) {
        if (test == null) {
            return;
        }
        test.log(passed ? Status.PASS : Status.FAIL, message);
    }

    public static String sanitizePayload(String payload) {
        String safePayload = maskSensitiveJson(payload == null ? "" : payload).trim();
        return truncate(safePayload.replaceAll("\\s+", " "), MAX_CONSOLE_PAYLOAD_CHARS);
    }

    private static String[][] summaryRows(HttpRequest request, HttpResponse<String> response, long responseTime) {
        return new String[][] {
                { "Field", "Value" },
                { "Method", request.method() },
                { "URL", request.uri().toString() },
                { "HTTP Status", String.valueOf(response.statusCode()) },
                { "Response Time", formatDuration(responseTime) },
                { "Content Type", ApiHelper.getContentType(response) }
        };
    }

    private static String[][] headerRows(HttpHeaders headers) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Header", "Value" });
        headers.map()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase(Locale.ROOT)))
                .forEach(entry -> rows.add(new String[] {
                        entry.getKey(),
                        isSensitiveKey(entry.getKey()) ? MASK : String.join(", ", entry.getValue())
                }));
        return rows.toArray(String[][]::new);
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

    private static ExtentColor statusColor(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return ExtentColor.GREEN;
        }
        if (statusCode >= 300 && statusCode < 400) {
            return ExtentColor.ORANGE;
        }
        if (statusCode >= 400 && statusCode < 500) {
            return ExtentColor.ORANGE;
        }
        if (statusCode >= 500) {
            return ExtentColor.RED;
        }
        return ExtentColor.GREY;
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

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "... [truncated, total chars: " + value.length() + "]";
    }
}
