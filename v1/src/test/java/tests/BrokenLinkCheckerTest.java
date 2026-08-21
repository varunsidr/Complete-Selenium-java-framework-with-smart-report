package tests;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import utils.BrokenLinkChecker;
import utils.Reporter;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.StaleElementReferenceException;

public class BrokenLinkCheckerTest extends BaseTest {

    private static class LinkCandidate {
        private final String href;
        private final WebElement element;
        private final int linkNumber;

        private LinkCandidate(String href, WebElement element, int linkNumber) {
            this.href = href;
            this.element = element;
            this.linkNumber = linkNumber;
        }
    }

    @Override
    protected boolean shouldNavigateToDefaultUrl() {
        return false;
    }

    @Override
    protected boolean shouldCreateExtentReport() {
        return false;
    }

    @Test(description = "Open the configured URL and verify all links return a successful HTTP response", groups = {"smoke", "broken-links"})
    public void verifyBrokenLinksOnPage() throws IOException {
        String targetUrl = prop.getProperty("url2", driver.getCurrentUrl());
        driver.get(targetUrl);
        String baseUrl = driver.getCurrentUrl();
        System.out.println("[BrokenLinkCheckerTest] Opening page and scanning links on: " + baseUrl);
        logToReport(Status.INFO, "Scanning links on: " + baseUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("a")));

        List<WebElement> links = driver.findElements(By.tagName("a"));
        List<Reporter.BrokenLinkResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        System.out.println("[BrokenLinkCheckerTest] Found " + links.size() + " anchor tags.");
        logToReport(Status.INFO, "Total anchor tags found: " + links.size());

        URI baseUri = URI.create(baseUrl);
        int connectTimeoutMillis = Integer.parseInt(prop.getProperty("broken.link.connect.timeout.millis", "3000"));
        int readTimeoutMillis = Integer.parseInt(prop.getProperty("broken.link.read.timeout.millis", "3000"));
        List<LinkCandidate> candidates = new ArrayList<>();
        for (WebElement link : links) {
            try {
                String href = link.getAttribute("href");
                if (href == null || href.trim().isEmpty()) {
                    continue;
                }
                String normalized = href.trim();
                if (normalized.startsWith("javascript:") || normalized.startsWith("mailto:") || normalized.startsWith("tel:")) {
                    continue;
                }
                if (!seen.add(normalized)) {
                    continue;
                }
                candidates.add(new LinkCandidate(normalized, link, candidates.size() + 1));
            } catch (StaleElementReferenceException e) {
                // ignore stale anchors and continue with remaining hrefs
            }
        }

        List<Callable<Reporter.BrokenLinkResult>> tasks = new ArrayList<>();
        for (LinkCandidate candidate : candidates) {
            String normalized = candidate.href;
            int linkNumber = candidate.linkNumber;
            tasks.add(() -> {
                System.out.println("[BrokenLinkCheckerTest] Checking link " + linkNumber + " of " + candidates.size() + ": " + normalized);
                return BrokenLinkChecker.check(normalized, baseUri, connectTimeoutMillis, readTimeoutMillis);
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            List<Future<Reporter.BrokenLinkResult>> futures = executor.invokeAll(tasks);
            for (int i = 0; i < futures.size(); i++) {
                Future<Reporter.BrokenLinkResult> future = futures.get(i);
                LinkCandidate candidate = candidates.get(i);
                Reporter.BrokenLinkResult result;
                try {
                    result = future.get(1500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    result = new Reporter.BrokenLinkResult("unknown", "ERROR", false, "Timeout or execution error: " + e.getMessage());
                }
                if (!result.isPassed()) {
                    result = attachFailedLinkScreenshot(result, candidate);
                }
                results.add(result);
                if (result.isPassed()) {
                    logToReport(Status.PASS, "Working link: " + result.getUrl() + " (" + result.getStatus() + ")");
                } else {
                    logToReport(Status.FAIL, "Broken link: " + result.getUrl() + " (" + result.getStatus() + ")");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Link checking interrupted", e);
        } finally {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        logToReport(Status.INFO, "Validated " + results.size() + " unique links");

        long failedCount = results.stream().filter(result -> !result.isPassed()).count();
        String reportPath = Reporter.generateBrokenLinkReport("broken-link-checker", baseUrl, results);
        System.out.println("[BrokenLinkCheckerTest] Broken link report generated at: " + reportPath);
        logToReport(Status.INFO, "Custom broken link report: " + reportPath);

        if (failedCount > 0) {
            logToReport(Status.WARNING, failedCount + " broken links were found. See report for details.");
        } else {
            logToReport(Status.PASS, "All checked links responded successfully");
        }
    }

    private Reporter.BrokenLinkResult attachFailedLinkScreenshot(Reporter.BrokenLinkResult result, LinkCandidate candidate) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                    candidate.element);
            String screenshotPath = Reporter.captureElementScreenShot(
                    candidate.element,
                    "BrokenLink_" + candidate.linkNumber + "_");
            System.out.println("[BrokenLinkCheckerTest] Failed link screenshot captured at: " + screenshotPath);
            return result.withScreenshotPath(screenshotPath);
        } catch (Exception e) {
            System.out.println("[BrokenLinkCheckerTest] Failed to capture element screenshot for link "
                    + candidate.linkNumber + ": " + e.getMessage());
            return result;
        }
    }
}
