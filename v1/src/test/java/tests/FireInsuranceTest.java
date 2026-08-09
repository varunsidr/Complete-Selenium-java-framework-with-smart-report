package tests;

import base.BaseTest;
import Pages.FireHomePage;
import Pages.FireUserDetails;
import org.testng.annotations.Test;

public class FireInsuranceTest extends BaseTest {

    FireHomePage homePage = new FireHomePage();
    FireUserDetails userDetails = new FireUserDetails();

    @Test(description = "User browses and checks Fire Insurance plans for their shop",
          groups = { "smoke", "fire-insurance" })
    public void verifyFireInsurancePlansForShop() {
        homePage.hoverOverInsuranceProducts(getTest());
        userDetails.enterMobileNumber("7311847321", getTest());
        userDetails.selectBusinessTypeAsShop(getTest());
        userDetails.checkPlansFor20Lakhs(getTest());
        userDetails.takeScreenshotOfAvailablePlan(getTest());
    }
}
