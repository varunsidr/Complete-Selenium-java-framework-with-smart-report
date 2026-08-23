package uistore;

import org.openqa.selenium.By;

public class Fire_locators {

    // FireHomePage
    public static final By INSURANCE_PRODUCTS = By.xpath("//a[normalize-space()='Insurance Products']");
    public static final By FIRE_INSURANCE = By.cssSelector("a[onclick*='Top Navigation'][href='https://www.policybazaar.com/commercial-insurance/fire-insurance/']");

    // FireUserDetails
    public static final By MOBILE_FIELD = By.xpath("(//input[@id='smemobile_hi'])[1]");
    public static final By SHOP_BUSINESS_TYPE = By.xpath("//li[@data-testid='option-3135']");
    public static final By TWENTY_LAKHS_PLAN = By.xpath("//li[@id='sumInsured-2000000']");
}
