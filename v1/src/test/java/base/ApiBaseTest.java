package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.Reporter;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ApiBaseTest {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Long> testStartTime = new ThreadLocal<>();

    private static synchronized void initExtent() {
        if (extent == null) {
            extent = Reporter.generateExtentReport("Hybrid_Framework_Report");
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setupApiReport(Method method, Object[] testData) {
        initExtent();
        ExtentTest test = createApiTest(method, testData);
        testThreadLocal.set(test);
        testStartTime.set(System.nanoTime());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownApiReport(ITestResult result) {
        ExtentTest currentTest = testThreadLocal.get();
        long durationMillis = elapsedMillis();
        if (currentTest != null) {
            if (result.getStatus() == ITestResult.SUCCESS) {
                currentTest.log(Status.PASS, "API scenario passed in " + durationMillis + " ms");
            } else if (result.getStatus() == ITestResult.FAILURE) {
                String failureMessage = result.getThrowable() == null
                        ? "API test failed"
                        : result.getThrowable().getMessage();
                currentTest.log(Status.FAIL, failureMessage + " | Duration: " + durationMillis + " ms");
            } else if (result.getStatus() == ITestResult.SKIP) {
                currentTest.log(Status.SKIP, "API scenario skipped after " + durationMillis + " ms");
            }
        }
        testThreadLocal.remove();
        testStartTime.remove();
    }

    @AfterSuite(alwaysRun = true)
    public static void flushApiReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    private static ExtentTest createApiTest(Method method, Object[] testData) {
        Test annotation = method.getAnnotation(Test.class);
        String description = annotation == null ? "" : annotation.description();
        String testName = description == null || description.isBlank()
                ? toDisplayName(method.getName())
                : description;
        String scenarioName = scenarioNameFrom(testData);
        if (!scenarioName.isBlank()) {
            testName = testName + " - " + scenarioName;
        }

        ExtentTest test = description == null || description.isBlank()
                ? extent.createTest(testName)
                : extent.createTest(testName, method.getDeclaringClass().getSimpleName() + "." + method.getName());

        test.assignCategory("API");
        if (annotation != null && annotation.groups().length > 0) {
            test.assignCategory(Arrays.stream(annotation.groups())
                    .map(String::toUpperCase)
                    .toArray(String[]::new));
        }
        test.assignAuthor(System.getProperty("user.name", "qa"));
        test.assignDevice("HTTP API");
        test.log(Status.INFO, "Test class: " + method.getDeclaringClass().getName());
        if (!scenarioName.isBlank()) {
            test.log(Status.INFO, "Scenario: " + scenarioName);
        }
        return test;
    }

    private static long elapsedMillis() {
        Long startTime = testStartTime.get();
        if (startTime == null) {
            return 0L;
        }
        return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private static String toDisplayName(String methodName) {
        return methodName
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[_-]+", " ")
                .trim();
    }

    private static String scenarioNameFrom(Object[] testData) {
        if (testData == null || testData.length == 0 || !(testData[0] instanceof String scenarioName)) {
            return "";
        }
        return scenarioName == null ? "" : scenarioName.trim();
    }
}
