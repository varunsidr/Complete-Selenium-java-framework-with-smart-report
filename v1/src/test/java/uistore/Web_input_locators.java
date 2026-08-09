package uistore;

import org.openqa.selenium.By;

public class Web_input_locators {

    public static final By CLEAR_BTN = By.xpath("//button[normalize-space()='Clear Inputs']");
    public static final By DISPLAY_BTN = By.xpath("//button[normalize-space()='Display Inputs']");

    public static final By INPUT_NUMBER = By.id("input-number");
    public static final By INPUT_TEXT = By.id("input-text");
    public static final By INPUT_PASSWORD = By.id("input-password");
    public static final By INPUT_DATE = By.id("input-date");

    public static final By RESULT_SECTION = By.id("result");
    public static final By OUTPUT_NUMBER = By.id("output-number");
    public static final By OUTPUT_TEXT = By.id("output-text");
    public static final By OUTPUT_PASSWORD = By.id("output-password");
    public static final By OUTPUT_DATE = By.id("output-date");

}
