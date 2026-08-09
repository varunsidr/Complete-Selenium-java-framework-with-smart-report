package tests;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Reporter;

public class SmokeTest extends BaseTest {

    @Test(description = "Verify the application URL loads successfully", groups = { "smoke" })
    public void verifyApplicationLoads() {
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();

        getTest().log(Status.INFO, "URL: " + currentUrl);
        getTest().log(Status.INFO, "Page Title: " + pageTitle);
        Reporter.attachScreenshotToReport("Application Loaded", getTest(), "Page loaded at: " + currentUrl);

        Assert.assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
        getTest().log(Status.PASS, "Application loaded successfully — " + pageTitle);
    }
}
