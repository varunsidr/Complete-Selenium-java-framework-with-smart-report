package Pages;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import utils.Base;
import utils.LoggerHandler;
import utils.Reporter;
import utils.WebDriverHelper;
import uistore.Fire_locators;

public class FireHomePage extends Base {

    WebDriverHelper helper = new WebDriverHelper();
    LoggerHandler logging = new LoggerHandler();

    public void hoverOverInsuranceProducts(ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Fire_locators.INSURANCE_PRODUCTS, WAIT_TIMEOUT);
            helper.hoverOverElement(Fire_locators.INSURANCE_PRODUCTS);

            helper.waitForElementToBeVisible(Fire_locators.FIRE_INSURANCE, WAIT_TIMEOUT);
            helper.javascriptScroll(Fire_locators.FIRE_INSURANCE);
            helper.jsclick(Fire_locators.FIRE_INSURANCE);

            logging.info("Hovered over Insurance Products and clicked Fire Insurance");
            test.log(Status.PASS, "Hovered over Insurance Products and clicked Fire Insurance successfully");
            Reporter.attachScreenshotToReport("Fire Insurance Selected", test,
                    "Successfully navigated to Fire Insurance");
        } catch (Exception e) {
            logging.error("Failed to navigate to Fire Insurance: " + e.getMessage());
            test.log(Status.FAIL, "Failed to navigate to Fire Insurance — " + e.getMessage());
            Reporter.attachScreenshotToReport("Fire Insurance Navigation Failed", test,
                    "Failed to navigate to Fire Insurance");
            throw new RuntimeException(e);
        }
    }
}
