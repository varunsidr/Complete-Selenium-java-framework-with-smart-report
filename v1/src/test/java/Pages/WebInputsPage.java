package Pages;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import utils.Base;
import utils.LoggerHandler;
import utils.WebDriverHelper;
import uistore.Web_input_locators;

public class WebInputsPage extends Base {

    WebDriverHelper helper = new WebDriverHelper();

    LoggerHandler logging = new LoggerHandler();
    

    public void killAds(ExtentTest test) {
        try {
            String script = "var selectors = ["
                    + "  'iframe[src*=\"ads\"]', 'iframe[src*=\"doubleclick\"]', 'iframe[src*=\"googlesyndication\"]',"
                    + "  'iframe[id*=\"google_ads\"]', 'iframe[id*=\"aswift\"]', 'ins.adsbygoogle',"
                    + "  '[id*=\"ad-\"]', '[class*=\"adsbygoogle\"]', '[class*=\"advertisement\"]',"
                    + "  '[id*=\"banner\"]', '[class*=\"cookie\"]', '[class*=\"consent\"]',"
                    + "  '[aria-label*=\"cookie\" i]'"
                    + "];"
                    + "var removed = 0;"
                    + "selectors.forEach(function(sel){"
                    + "  document.querySelectorAll(sel).forEach(function(el){ el.remove(); removed++; });"
                    + "});"
                    + "return removed;";
            Object removed = ((JavascriptExecutor) driver).executeScript(script);
            logging.info("Ad-killer removed " + removed + " element(s)");
            test.log(Status.INFO, "Ad-killer removed " + removed + " element(s)");
        } catch (Exception e) {
            logging.error("Ad-killer error (ignored): " + e.getMessage());
            test.log(Status.INFO, "Ad-killer skipped: " + e.getMessage());
        }
    }

    public void clickClearInputs(ExtentTest test) {
        try {
            helper.waitForElementToBeClickable(Web_input_locators.CLEAR_BTN, WAIT_TIMEOUT, "Clear Inputs button");
            helper.javascriptScroll(Web_input_locators.CLEAR_BTN, "Clear Inputs button");
            helper.jsclick(Web_input_locators.CLEAR_BTN, "Clear Inputs button");
            test.log(Status.INFO, "Clicked Clear Inputs button");
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to click Clear Inputs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fillNumber(String value, ExtentTest test) {
        try {
            helper.sendKeys(Web_input_locators.INPUT_NUMBER, value, "Number field");
            test.log(Status.INFO, "Filled Number field: " + value);
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to fill Number field: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fillText(String value, ExtentTest test) {
        try {
            helper.sendKeys(Web_input_locators.INPUT_TEXT, value, "Text field");
            test.log(Status.INFO, "Filled Text field: " + value);
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to fill Text field: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fillPassword(String value, ExtentTest test) {
        try {
            helper.sendKeys(Web_input_locators.INPUT_PASSWORD, value, "Password field");
            test.log(Status.INFO, "Filled Password field");
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to fill Password field: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Date must be yyyy-MM-dd in Excel. sendKeys is unreliable on <input
    // type="date">, so JS sets it.
    public void fillDate(String value, ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(Web_input_locators.INPUT_DATE, WAIT_TIMEOUT, "Date field");
            WebElement dateField = driver.findElement(Web_input_locators.INPUT_DATE);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value = arguments[1]", dateField, value);
            js.executeScript("arguments[0].dispatchEvent(new Event('change', {bubbles:true}))", dateField);
            logging.info("Filled Date field: " + value);
            test.log(Status.INFO, "Filled Date field: " + value);
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to fill Date field: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void clickDisplayInputs(ExtentTest test) {
        try {
            helper.waitForElementToBeClickable(Web_input_locators.DISPLAY_BTN, WAIT_TIMEOUT, "Display Inputs button");
            helper.javascriptScroll(Web_input_locators.DISPLAY_BTN, "Display Inputs button");
            helper.jsclick(Web_input_locators.DISPLAY_BTN, "Display Inputs button");
            test.log(Status.INFO, "Clicked Display Inputs button");
        } catch (Exception e) {
            test.log(Status.FAIL, "Failed to click Display Inputs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** Clears the form, fills all four fields from one data row, then clicks Display Inputs. */
    public void submit(String number, String text, String password, String date, ExtentTest test) {
        clickClearInputs(test);
        fillNumber(number, test);
        fillText(text, test);
        fillPassword(password, test);
        fillDate(date, test);
        clickDisplayInputs(test);
    }

    /** Reads the four echoed output fields and returns them as {number, text, password, date}. */
    public String[] readOutputs(ExtentTest test) {
        return new String[] {
            getOutputNumber(test),
            getOutputText(test),
            getOutputPassword(test),
            getOutputDate(test)
        };
    }

    public String getOutputNumber(ExtentTest test) {
        helper.waitForElementToBeVisible(Web_input_locators.RESULT_SECTION, WAIT_TIMEOUT, "Result section");
        helper.javascriptScroll(Web_input_locators.RESULT_SECTION, "Result section");
        return readOutput(Web_input_locators.OUTPUT_NUMBER, "Output Number field", test);
    }

    public String getOutputText(ExtentTest test) {
        return readOutput(Web_input_locators.OUTPUT_TEXT, "Output Text field", test);
    }

    public String getOutputPassword(ExtentTest test) {
        return readOutput(Web_input_locators.OUTPUT_PASSWORD, "Output Password field", test);
    }

    public String getOutputDate(ExtentTest test) {
        return readOutput(Web_input_locators.OUTPUT_DATE, "Output Date field", test);
    }

    private String readOutput(By locator, String label, ExtentTest test) {
        try {
            helper.waitForElementToBeVisible(locator, WAIT_TIMEOUT, label);
            WebElement el = driver.findElement(locator);
            String value = el.getAttribute("value");
            if (value == null || value.isEmpty())
                value = el.getText();
            value = value != null ? value.trim() : "";
            logging.info(label + ": " + value);
            test.log(Status.INFO, label + ": " + value);
            return value;
        } catch (Exception e) {
            logging.error("Could not read " + label + ": " + e.getMessage());
            test.log(Status.INFO, label + " not captured: " + e.getMessage());
            return "";
        }
    }
}
