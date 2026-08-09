package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import utils.Base;
import utils.Reporter;

import java.io.File;
import java.lang.reflect.Method;

public class BaseTest extends Base {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    private static synchronized void initExtent() {
        if (extent == null) {
            File screenshotsDir = new File(System.getProperty("user.dir") + "/reports/screenshots");
            if (screenshotsDir.exists()) {
                File[] files = screenshotsDir.listFiles();
                if (files != null) {
                    for (File f : files) f.delete();
                }
            }
            extent = Reporter.generateExtentReport("Hybrid_Framework_Report");
        }
    }

    protected boolean shouldNavigateToDefaultUrl() {
        return true;
    }

    protected boolean shouldCreateExtentReport() {
        return true;
    }

    protected void logToReport(Status status, String message) {
        ExtentTest currentTest = testThreadLocal.get();
        if (currentTest != null) {
            currentTest.log(status, message);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setupBrowser(Method method) {
        try {
            boolean createReport = shouldCreateExtentReport();
            if (createReport) {
                initExtent();
            }
            Base.openBrowser(shouldNavigateToDefaultUrl());
            if (createReport) {
                testThreadLocal.set(extent.createTest(method.getName()));
            } else {
                testThreadLocal.remove();
            }
        } catch (Exception e) {
            throw new RuntimeException("Browser setup failed: " + e.getMessage(), e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            ExtentTest currentTest = testThreadLocal.get();
            if (result.getStatus() == ITestResult.FAILURE) {
                String failureMessage = result.getThrowable() == null ? "Test failed" : result.getThrowable().getMessage();
                if (currentTest != null) {
                    currentTest.log(Status.FAIL, failureMessage);
                    Reporter.attachFailureScreenshotToReport("Failure", currentTest, failureMessage);
                } else {
                    String screenshotPath = Reporter.captureScreenShot("Failure");
                    System.out.println("[BaseTest] Failure screenshot captured at: " + screenshotPath);
                }
            } else if (currentTest != null && result.getStatus() == ITestResult.SKIP) {
                currentTest.log(Status.SKIP, "Test skipped");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Base.hasDriver()) {
                Base.quitDriver();
            }
            testThreadLocal.remove();
        }
    }

    @AfterSuite(alwaysRun = true)
    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }
}
