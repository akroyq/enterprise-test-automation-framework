package com.qualityarchitect.core.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class UtilitiesTest {

    @Test(groups = {"unit", "utils"}, description = "Validate DataGeneratorUtil methods")
    public void testDataGeneratorUtil() {
        String policyId = DataGeneratorUtil.generatePolicyId();
        Assert.assertTrue(policyId.startsWith("POL-"), "Policy ID should start with POL-");

        String email = DataGeneratorUtil.generateEmail();
        Assert.assertTrue(email.contains("@") && email.contains(".com"), "Invalid email format");

        int amount = DataGeneratorUtil.generateClaimAmount(100, 5000);
        Assert.assertTrue(amount >= 100 && amount <= 5000, "Amount out of range");

        int deductible = DataGeneratorUtil.generateDeductible(amount);
        Assert.assertTrue(deductible <= amount, "Deductible exceeds claim amount");

        String fullName = DataGeneratorUtil.generateFullName();
        Assert.assertFalse(fullName.isBlank(), "Full name should not be blank");
    }

    @Test(groups = {"unit", "utils"}, description = "Validate EmailUtil report dispatch simulation")
    public void testEmailUtilDispatch() {
        boolean sent = EmailUtil.sendExecutionReport(
                "stakeholders@insurance-claims.com",
                "Common Suite",
                "target/extent-reports/ExtentReport.html",
                Map.of("Total Tests", "10", "Passed", "10", "Failed", "0")
        );
        Assert.assertTrue(sent, "Email dispatch simulation should return true");
    }
}
