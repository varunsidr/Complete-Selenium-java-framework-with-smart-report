package Pages;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import uistore.Fire_locators;
import utils.Base;
import utils.LoggerHandler;
import utils.Reporter;
import utils.WebDriverHelper;

public class FireHomePage extends Base {

    WebDriverHelper helper = new WebDriverHelper();
    LoggerHandler logging = new LoggerHandler();

    public void hoverOverInsuranceProducts(ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Fire_locators.INSURANCE_PRODUCTS, WAIT_TIMEOUT);
            helper.hoverOverElement(Fire_locators.INSURANCE_PRODUCTS);

            helper.hoverAndClick(Fire_locators.FIRE_INSURANCE, "Fire Insurance");
            helper.waitForUrlToContain("/commercial-insurance/fire-insurance/", WAIT_TIMEOUT);
            helper.waitForElementToBeVisible(Fire_locators.MOBILE_FIELD, WAIT_TIMEOUT, "Fire Insurance mobile field");

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
