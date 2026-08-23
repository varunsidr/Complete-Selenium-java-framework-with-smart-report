package Pages;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import uistore.Fire_locators;
import utils.Base;
import utils.LoggerHandler;
import utils.Reporter;
import utils.WebDriverHelper;

public class FireUserDetails extends Base {

    WebDriverHelper helper = new WebDriverHelper();
    LoggerHandler logging = new LoggerHandler();

    public void enterMobileNumber(String number, ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Fire_locators.MOBILE_FIELD, WAIT_TIMEOUT);
            helper.sendKeys(Fire_locators.MOBILE_FIELD, number);

            logging.info("Entered mobile number: " + number);
            test.log(Status.PASS, "Entered mobile number successfully");
            Reporter.attachScreenshotToReport("Mobile Number Entered", test,
                    "Entered mobile number: " + number);
        } catch (Exception e) {
            logging.error("Failed to enter mobile number: " + e.getMessage());
            test.log(Status.FAIL, "Failed to enter mobile number — " + e.getMessage());
            Reporter.attachScreenshotToReport("Mobile Number Entry Failed", test,
                    "Failed to enter mobile number");
            throw new RuntimeException(e);
        }
    }

    public void selectBusinessTypeAsShop(ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Fire_locators.SHOP_BUSINESS_TYPE, WAIT_TIMEOUT);
            helper.clickingOnElement(Fire_locators.SHOP_BUSINESS_TYPE);
            helper.waitForElementToBeVisible(Fire_locators.SHOP_PLAN_HEADING, WAIT_TIMEOUT, "Shop plan page heading");

            logging.info("Selected business type as Shops");
            test.log(Status.PASS, "Selected business type as Shops successfully");
            Reporter.attachScreenshotToReport("Shop Type Selected", test,
                    "Selected business type as Shops");
        } catch (Exception e) {
            logging.error("Failed to select business type as Shops: " + e.getMessage());
            test.log(Status.FAIL, "Failed to select business type as Shops — " + e.getMessage());
            Reporter.attachScreenshotToReport("Shop Type Selection Failed", test,
                    "Failed to select business type as Shops");
            throw new RuntimeException(e);
        }
    }

    public void checkPlansFor20Lakhs(ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Fire_locators.TWENTY_LAKHS_PLAN, WAIT_TIMEOUT);
            helper.clickingOnElement(Fire_locators.TWENTY_LAKHS_PLAN);

            logging.info("Selected 20 Lakhs plan");
            test.log(Status.PASS, "Selected 20 Lakhs plan successfully");
            Reporter.attachScreenshotToReport("20 Lakhs Plan Selected", test,
                    "Selected 20 Lakhs plan");
        } catch (Exception e) {
            logging.error("Failed to select 20 Lakhs plan: " + e.getMessage());
            test.log(Status.FAIL, "Failed to select 20 Lakhs plan — " + e.getMessage());
            Reporter.attachScreenshotToReport("20 Lakhs Plan Selection Failed", test,
                    "Failed to select 20 Lakhs plan");
            throw new RuntimeException(e);
        }
    }

    public void takeScreenshotOfAvailablePlan(ExtentTest test) {
        try {
            logging.info("Took screenshot of available plans");
            test.log(Status.PASS, "Took screenshot of available plans successfully");
            Reporter.attachScreenshotToReport("Available Plans Screenshot", test,
                    "Screenshot of available Fire Insurance plans");
        } catch (Exception e) {
            logging.error("Failed to take screenshot: " + e.getMessage());
            test.log(Status.FAIL, "Failed to take screenshot — " + e.getMessage());
            Reporter.attachScreenshotToReport("Screenshot Failed", test, "Failed to take screenshot");
            throw new RuntimeException(e);
        }
    }
}
