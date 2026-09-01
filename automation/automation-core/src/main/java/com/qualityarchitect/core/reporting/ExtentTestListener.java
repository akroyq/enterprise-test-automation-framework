package com.qualityarchitect.core.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ExtentTestListener implements ITestListener {
    private static ExtentReports extentReports;

    @Override
    public void onStart(ITestContext context) {
        String suiteName = context.getSuite().getName();
        extentReports = ExtentReportManager.getInstance(suiteName);
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        ExtentTest test = extentReports.createTest(testName, description);
        test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
        for (String group : result.getMethod().getGroups()) {
            test.assignCategory(group);
        }

        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            test.info("Test Parameters: " + Arrays.toString(parameters));
        }

        ExtentReportManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " PASSED", ExtentColor.GREEN));
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " FAILED", ExtentColor.RED));
            if (result.getThrowable() != null) {
                test.log(Status.FAIL, result.getThrowable());
            }

            attachFailureScreenshotIfAvailable(result.getInstance(), test);
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " SKIPPED", ExtentColor.ORANGE));
            if (result.getThrowable() != null) {
                test.log(Status.SKIP, result.getThrowable());
            }
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flushReports();
    }

    private void attachFailureScreenshotIfAvailable(Object testInstance, ExtentTest test) {
        if (testInstance == null || test == null) {
            return;
        }
        try {
            Method getDriverMethod = testInstance.getClass().getMethod("getDriver");
            Object driverObj = getDriverMethod.invoke(testInstance);
            if (driverObj != null) {
                Class<?> takesScreenshotClass = Class.forName("org.openqa.selenium.TakesScreenshot");
                Class<?> outputTypeClass = Class.forName("org.openqa.selenium.OutputType");
                Object base64Type = outputTypeClass.getField("BASE64").get(null);

                if (takesScreenshotClass.isInstance(driverObj)) {
                    Method getScreenshotAsMethod = takesScreenshotClass.getMethod("getScreenshotAs", outputTypeClass);
                    Object res = getScreenshotAsMethod.invoke(driverObj, base64Type);
                    if (res instanceof String base64 && !base64.isEmpty()) {
                        test.fail("Failure Screenshot:",
                                MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                    }
                }
            }
        } catch (Throwable ignored) {
            // Not a UI test or WebDriver not active
        }
    }
}
