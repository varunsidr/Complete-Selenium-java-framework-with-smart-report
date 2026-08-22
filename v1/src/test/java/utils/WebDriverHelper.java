package utils;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;

import base.BaseTest;

public class WebDriverHelper extends Base {

    private LoggerHandler logging = new LoggerHandler();
    private static final int ANIMATION_WAIT_MS = 500;

    /** Takes an INFO-level screenshot and attaches it to the active ExtentTest.
     *  Uses the label as both the file name prefix and the report step message.
     *  Usage: captureAction("Clicked: Submit button"); */
    private void captureAction(String label) {
        try {
            ExtentTest test = BaseTest.getTest();
            if (test != null) Reporter.attachInfoScreenshotToReport(label, test, label);
        } catch (Exception e) {
            logging.error("Failed to attach action screenshot [" + label + "]: " + e.getMessage());
        }
    }

    /** Takes a FAIL-level screenshot at the exact moment an action throws and attaches it to the report.
     *  Captures browser state before the exception propagates, giving a forensic snapshot of the failure.
     *  Usage: captureFailure("JS click: Submit button", e); */
    private void captureFailure(String label, Exception cause) {
        try {
            ExtentTest test = BaseTest.getTest();
            if (test != null) {
                String message = "FAILED — " + label + ": " + cause.getMessage();
                Reporter.attachFailureScreenshotToReport(label + "_FAILED", test, message);
            }
        } catch (Exception e) {
            logging.error("Failed to attach failure screenshot [" + label + "]: " + e.getMessage());
        }
    }

    // ── Waits ────────────────────────────────────────────────────────────────

    /** Waits up to timeoutInSeconds for the element to be visible in the DOM.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.waitForElementToBeVisible(SUBMIT_BTN, 10); */
    public void waitForElementToBeVisible(By locator, int timeoutInSeconds) {
        waitForElementToBeVisible(locator, timeoutInSeconds, locator.toString());
    }

    /** Waits up to timeoutInSeconds for the element to be visible in the DOM.
     *  'name' is shown in the failure screenshot label instead of the raw locator.
     *  Usage: helper.waitForElementToBeVisible(SUBMIT_BTN, 10, "Submit button"); */
    public void waitForElementToBeVisible(By locator, int timeoutInSeconds, String name) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            logging.error("Element not visible [" + name + "]: " + e.getMessage());
            captureFailure("Wait visible: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits up to timeoutInSeconds for the element to be clickable (visible + enabled).
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.waitForElementToBeClickable(SUBMIT_BTN, 10); */
    public void waitForElementToBeClickable(By locator, int timeoutInSeconds) {
        waitForElementToBeClickable(locator, timeoutInSeconds, locator.toString());
    }

    /** Waits up to timeoutInSeconds for the element to be clickable (visible + enabled).
     *  'name' is shown in the failure screenshot label instead of the raw locator.
     *  Usage: helper.waitForElementToBeClickable(SUBMIT_BTN, 10, "Submit button"); */
    public void waitForElementToBeClickable(By locator, int timeoutInSeconds, String name) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            logging.error("Element not clickable [" + name + "]: " + e.getMessage());
            captureFailure("Wait clickable: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits up to timeoutInSeconds for the element to leave the DOM or become invisible.
     *  Useful after clicking a button that triggers a loading spinner or modal dismissal.
     *  Usage: helper.waitForElementToDisappear(SPINNER, 10); */
    public void waitForElementToDisappear(By locator, int timeoutInSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            logging.error("Element did not disappear [" + locator + "]: " + e.getMessage());
            captureFailure("Wait disappear: " + locator, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits up to timeoutInSeconds for the browser URL to contain partialUrl.
     *  Use after a click or form submit that triggers a page navigation.
     *  Usage: helper.waitForUrlToContain("dashboard", 10); */
    public void waitForUrlToContain(String partialUrl, int timeoutInSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.urlContains(partialUrl));
        } catch (Exception e) {
            logging.error("URL did not contain [" + partialUrl + "]: " + e.getMessage());
            captureFailure("Wait URL contains: " + partialUrl, e);
            throw new RuntimeException(e);
        }
    }

    /** Alias for waitForElementToBeVisible — kept for backward compatibility.
     *  Delegates directly to the standard visibility wait with the same timeout.
     *  Usage: helper.waitForElementToBeVisibleImplicitly(SUBMIT_BTN, 10); */
    public void waitForElementToBeVisibleImplicitly(By locator, int timeoutInSeconds) {
        waitForElementToBeVisible(locator, timeoutInSeconds);
    }

    // ── Clicks ───────────────────────────────────────────────────────────────

    /** Waits for the element to be clickable, then performs a standard Selenium click.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.clickingOnElement(SUBMIT_BTN); */
    public void clickingOnElement(By locator) {
        clickingOnElement(locator, locator.toString());
    }

    /** Waits for the element to be clickable, then performs a standard Selenium click.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.clickingOnElement(SUBMIT_BTN, "Submit button"); */
    public void clickingOnElement(By locator, String name) {
        try {
            waitForElementToBeClickable(locator, 5, name);
            driver.findElement(locator).click();
            logging.info("Clicked: " + name);
            captureAction("Clicked: " + name);
        } catch (Exception e) {
            logging.error("Failed to click [" + name + "]: " + e.getMessage());
            captureFailure("Click: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits for the element to be clickable, then fires a JavaScript click on it.
     *  Use instead of clickingOnElement when overlays or ads block the native click.
     *  Usage: helper.jsclick(SUBMIT_BTN); */
    public void jsclick(By locator) {
        jsclick(locator, locator.toString());
    }

    /** Waits for the element to be clickable, then fires a JavaScript click on it.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.jsclick(SUBMIT_BTN, "Submit button"); */
    public void jsclick(By locator, String name) {
        try {
            waitForElementToBeClickable(locator, 5, name);
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            logging.info("JS clicked: " + name);
            captureAction("JS clicked: " + name);
        } catch (Exception e) {
            logging.error("Failed to JS click [" + name + "]: " + e.getMessage());
            captureFailure("JS click: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Moves the mouse to the element using Actions, waits briefly for animations to settle.
     *  Use for dropdown menus or tooltips that only appear on hover.
     *  Usage: helper.hoverOverElement(NAV_MENU); */
    public void hoverOverElement(By locator) {
        hoverOverElement(locator, locator.toString());
    }

    /** Moves the mouse to the element using Actions, waits briefly for animations to settle.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.hoverOverElement(NAV_MENU, "Navigation menu"); */
    public void hoverOverElement(By locator, String name) {
        try {
            WebElement webElement = driver.findElement(locator);
            new Actions(Base.getDriver()).moveToElement(webElement).perform();
            Thread.sleep(ANIMATION_WAIT_MS);
            logging.info("Hovered over: " + name);
            captureAction("Hovered over: " + name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logging.error("Failed to hover over [" + name + "]: " + e.getMessage());
            captureFailure("Hover: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Moves to the element and clicks it in one Action chain.
     *  Use for hover-revealed menu items that collapse if the pointer leaves the menu.
     *  Usage: helper.hoverAndClick(FIRE_INSURANCE); */
    public void hoverAndClick(By locator) {
        hoverAndClick(locator, locator.toString());
    }

    /** Moves to the element and clicks it in one Action chain.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.hoverAndClick(FIRE_INSURANCE, "Fire Insurance"); */
    public void hoverAndClick(By locator, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            WebElement webElement = driver.findElement(locator);
            new Actions(Base.getDriver()).moveToElement(webElement).click().perform();
            logging.info("Hovered and clicked: " + name);
            captureAction("Hovered and clicked: " + name);
        } catch (Exception e) {
            logging.error("Failed to hover and click [" + name + "]: " + e.getMessage());
            captureFailure("Hover+Click: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Clicks the element, selects all text with Ctrl+A, then deletes it with Backspace.
     *  Use to clear an input that doesn't respond to the standard clear() method.
     *  Usage: helper.clickWithBackspace(SEARCH_BOX); */
    public void clickWithBackspace(By locator) {
        clickWithBackspace(locator, locator.toString());
    }

    /** Clicks the element, selects all text with Ctrl+A, then deletes it with Backspace.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.clickWithBackspace(SEARCH_BOX, "Search box"); */
    public void clickWithBackspace(By locator, String name) {
        try {
            waitForElementToBeClickable(locator, 5, name);
            WebElement webElement = driver.findElement(locator);
            webElement.click();
            webElement.sendKeys(Keys.CONTROL + "a");
            webElement.sendKeys(Keys.BACK_SPACE);
            logging.info("Clicked and cleared: " + name);
            captureAction("Clicked & cleared: " + name);
        } catch (Exception e) {
            logging.error("Failed to click+clear [" + name + "]: " + e.getMessage());
            captureFailure("Click+Clear: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits for the element to be clickable, then sends the Enter key to it.
     *  Use to submit forms or confirm dialogs without a dedicated submit button.
     *  Usage: helper.enterAction(SEARCH_BOX); */
    public void enterAction(By locator) {
        enterAction(locator, locator.toString());
    }

    /** Waits for the element to be clickable, then sends the Enter key to it.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.enterAction(SEARCH_BOX, "Search box"); */
    public void enterAction(By locator, String name) {
        try {
            waitForElementToBeClickable(locator, 5, name);
            driver.findElement(locator).sendKeys(Keys.ENTER);
            logging.info("Pressed Enter on: " + name);
            captureAction("Pressed Enter on: " + name);
        } catch (Exception e) {
            logging.error("Failed to press Enter on [" + name + "]: " + e.getMessage());
            captureFailure("Enter key: " + name, e);
            throw new RuntimeException(e);
        }
    }

    // ── Type / SendKeys ───────────────────────────────────────────────────────

    /** Clears the field with Ctrl+A + Backspace, then types the given data into it.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.sendKeys(INPUT_EMAIL, "test@email.com"); */
    public void sendKeys(By locator, String data) {
        sendKeys(locator, data, locator.toString());
    }

    /** Clears the field with Ctrl+A + Backspace, then types the given data into it.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.sendKeys(INPUT_EMAIL, "test@email.com", "Email field"); */
    public void sendKeys(By locator, String data, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            WebElement webElement = driver.findElement(locator);
            webElement.sendKeys(Keys.CONTROL + "a");
            webElement.sendKeys(Keys.BACK_SPACE);
            webElement.sendKeys(data);
            logging.info("Typed [" + data + "] into: " + name);
            captureAction("Typed into " + name + ": " + data);
        } catch (Exception e) {
            logging.error("Failed to type into [" + name + "]: " + e.getMessage());
            captureFailure("Type into: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Clears the field, types the given data, then presses Tab to move focus away.
     *  Use when the next field or validation only activates after a Tab key event.
     *  Usage: helper.sendKeysWithTab(INPUT_EMAIL, "test@email.com"); */
    public void sendKeysWithTab(By locator, String data) {
        sendKeysWithTab(locator, data, locator.toString());
    }

    /** Clears the field, types the given data, then presses Tab to move focus away.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.sendKeysWithTab(INPUT_EMAIL, "test@email.com", "Email field"); */
    public void sendKeysWithTab(By locator, String data, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            WebElement webElement = driver.findElement(locator);
            webElement.sendKeys(Keys.CONTROL + "a");
            webElement.sendKeys(Keys.BACK_SPACE);
            webElement.sendKeys(data + Keys.TAB);
            logging.info("Typed+Tab [" + data + "] into: " + name);
            captureAction("Typed into " + name + " (Tab): " + data);
        } catch (Exception e) {
            logging.error("Failed to type+Tab into [" + name + "]: " + e.getMessage());
            captureFailure("Type+Tab into: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Types the given data into the field and immediately presses Enter to submit.
     *  Use for search boxes or single-field forms that submit on Enter.
     *  Usage: helper.sendKeysWithEnter(SEARCH_BOX, "laptops"); */
    public void sendKeysWithEnter(By locator, String data) {
        sendKeysWithEnter(locator, data, locator.toString());
    }

    /** Types the given data into the field and immediately presses Enter to submit.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.sendKeysWithEnter(SEARCH_BOX, "laptops", "Search box"); */
    public void sendKeysWithEnter(By locator, String data, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            driver.findElement(locator).sendKeys(data + Keys.ENTER);
            logging.info("Typed+Enter [" + data + "] into: " + name);
            captureAction("Typed into " + name + " (Enter): " + data);
        } catch (Exception e) {
            logging.error("Failed to type+Enter into [" + name + "]: " + e.getMessage());
            captureFailure("Type+Enter into: " + name, e);
            throw new RuntimeException(e);
        }
    }

    // ── Scroll ───────────────────────────────────────────────────────────────

    /** Scrolls the element into view using JavaScript scrollIntoView().
     *  Use before clicking elements that are off-screen or hidden behind sticky headers.
     *  Usage: helper.javascriptScroll(FOOTER_BTN); */
    public void javascriptScroll(By locator) {
        javascriptScroll(locator, locator.toString());
    }

    /** Scrolls the element into view using JavaScript scrollIntoView().
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.javascriptScroll(FOOTER_BTN, "Footer button"); */
    public void javascriptScroll(By locator, String name) {
        try {
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
            logging.info("Scrolled to: " + name);
            captureAction("Scrolled to: " + name);
        } catch (Exception e) {
            logging.error("Failed to scroll to [" + name + "]: " + e.getMessage());
            captureFailure("Scroll to: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Scrolls to the element using Selenium Actions moveToElement (mouse-based scroll).
     *  Preferred when the page uses custom scroll listeners that JavaScript won't trigger.
     *  Usage: helper.scrollTo(FOOTER_BTN); */
    public void scrollTo(By locator) {
        scrollTo(locator, locator.toString());
    }

    /** Scrolls to the element using Selenium Actions moveToElement (mouse-based scroll).
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.scrollTo(FOOTER_BTN, "Footer button"); */
    public void scrollTo(By locator, String name) {
        try {
            WebElement elem = driver.findElement(locator);
            new Actions(Base.getDriver()).moveToElement(elem).build().perform();
            logging.info("Scrolled to: " + name);
            captureAction("Scrolled to: " + name);
        } catch (Exception e) {
            logging.error("Failed to scroll to [" + name + "]: " + e.getMessage());
            captureFailure("Scroll to: " + name, e);
            throw new RuntimeException(e);
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** Waits for the element to be visible, then returns its visible text via getText().
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.getText(ERROR_MSG); */
    public String getText(By locator) {
        return getText(locator, locator.toString());
    }

    /** Waits for the element to be visible, then returns its visible text via getText().
     *  'name' appears in logs and failure screenshots instead of the raw locator string.
     *  Usage: helper.getText(ERROR_MSG, "Error message"); */
    public String getText(By locator, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            String text = driver.findElement(locator).getText();
            logging.info("Got text from " + name + ": [" + text + "]");
            return text;
        } catch (Exception e) {
            logging.error("Failed to get text from [" + name + "]: " + e.getMessage());
            captureFailure("Get text: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Waits for the element to be visible, then returns the value of the named HTML attribute.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.getAttributeValue(INPUT_FIELD, "value"); */
    public String getAttributeValue(By locator, String attribute) {
        return getAttributeValue(locator, attribute, locator.toString());
    }

    /** Waits for the element to be visible, then returns the value of the named HTML attribute.
     *  'name' appears in logs and failure screenshots instead of the raw locator string.
     *  Usage: helper.getAttributeValue(INPUT_FIELD, "value", "Number field"); */
    public String getAttributeValue(By locator, String attribute, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            String value = driver.findElement(locator).getAttribute(attribute);
            logging.info("Got [" + attribute + "] from " + name + ": [" + value + "]");
            return value;
        } catch (Exception e) {
            logging.error("Failed to get [" + attribute + "] from [" + name + "]: " + e.getMessage());
            captureFailure("Get attribute [" + attribute + "]: " + name, e);
            throw new RuntimeException(e);
        }
    }

    // ── Dropdown ─────────────────────────────────────────────────────────────

    /** Selects an option from a native <select> dropdown by matching its value attribute.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.selectDropdownByValue(COUNTRY_DROP, "IN"); */
    public void selectDropdownByValue(By locator, String value) {
        selectDropdownByValue(locator, value, locator.toString());
    }

    /** Selects an option from a native <select> dropdown by matching its value attribute.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.selectDropdownByValue(COUNTRY_DROP, "IN", "Country dropdown"); */
    public void selectDropdownByValue(By locator, String value, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            new Select(driver.findElement(locator)).selectByValue(value);
            logging.info("Selected [" + value + "] in dropdown: " + name);
            captureAction("Selected [" + value + "] in: " + name);
        } catch (Exception e) {
            logging.error("Failed to select [" + value + "] in [" + name + "]: " + e.getMessage());
            captureFailure("Select dropdown: " + name, e);
            throw new RuntimeException(e);
        }
    }

    /** Selects an option from a native <select> dropdown by matching its visible display text.
     *  Raw locator string is used as the label — prefer the named overload for readable reports.
     *  Usage: helper.selectDropdownByVisibleText(COUNTRY_DROP, "India"); */
    public void selectDropdownByVisibleText(By locator, String text) {
        selectDropdownByVisibleText(locator, text, locator.toString());
    }

    /** Selects an option from a native <select> dropdown by matching its visible display text.
     *  'name' appears in the report step and screenshot instead of the raw XPath/ID.
     *  Usage: helper.selectDropdownByVisibleText(COUNTRY_DROP, "India", "Country dropdown"); */
    public void selectDropdownByVisibleText(By locator, String text, String name) {
        try {
            waitForElementToBeVisible(locator, 5, name);
            new Select(driver.findElement(locator)).selectByVisibleText(text);
            logging.info("Selected [" + text + "] in dropdown: " + name);
            captureAction("Selected [" + text + "] in: " + name);
        } catch (Exception e) {
            logging.error("Failed to select [" + text + "] in [" + name + "]: " + e.getMessage());
            captureFailure("Select dropdown: " + name, e);
            throw new RuntimeException(e);
        }
    }

    // ── Misc ─────────────────────────────────────────────────────────────────

    /** Checks whether the element is present in the DOM within a 3-second window.
     *  Returns true if found, false if not — never throws, so safe to use in if-checks.
     *  Usage: if (helper.isElementPresent(COOKIE_BANNER)) { ... } */
    public boolean isElementPresent(By locator) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            logging.info("Element present [" + locator + "]");
            return true;
        } catch (Exception e) {
            logging.info("Element not present [" + locator + "]");
            return false;
        }
    }

    /** Iterates all open window handles and switches the driver to the last one found.
     *  Use after clicking a link that opens a new tab or pop-up window.
     *  Usage: helper.switchToNewWindow(); */
    public void switchToNewWindow() {
        try {
            Set<String> windowHandles = driver.getWindowHandles();
            for (String windowHandle : windowHandles) {
                driver.switchTo().window(windowHandle);
            }
            logging.info("Switched to new window");
        } catch (Exception e) {
            logging.error("Failed to switch to new window: " + e.getMessage());
            captureFailure("Switch to new window", e);
            throw new RuntimeException(e);
        }
    }
}
