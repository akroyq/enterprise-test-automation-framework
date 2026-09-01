package com.qualityarchitect.ui.tests;

import com.qualityarchitect.ui.pages.ClaimsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClaimsUiTest extends BaseUiTest {

    @Test(
        groups = {"smoke", "regression", "p0", "ui", "claim-creation"},
        description = "Validate claim creation via UI portal displays SUBMITTED status and generated claim ID"
    )
    public void createClaimShouldShowSubmittedStatus() {
        ClaimsPage page = new ClaimsPage(getDriver())
            .open(baseUrl())
            .enterClaim("POL-1001", "auto", "1000", "100")
            .submit();

        Assert.assertEquals(page.status(), "SUBMITTED");
        Assert.assertFalse(page.claimId().isBlank());
    }

    @Test(
        groups = {"smoke", "regression", "p0", "ui", "e2e"},
        description = "Validate end-to-end user flow: create claim and calculate settlement through UI portal"
    )
    public void crossFunctionalFlowCreateThenCalculateShouldShowSettlement() {
        ClaimsPage page = new ClaimsPage(getDriver())
            .open(baseUrl())
            .enterClaim("POL-1001", "auto", "1000", "100")
            .submit()
            .calculate();

        Assert.assertEquals(page.settlement(), "900");
    }
}
