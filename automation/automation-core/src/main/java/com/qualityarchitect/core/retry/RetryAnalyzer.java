package com.qualityarchitect.core.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = Integer.parseInt(
            System.getProperty("maxRetryCount", System.getenv().getOrDefault("MAX_RETRY_COUNT", "2"))
    );

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            System.out.println("[RETRY] Retrying test '" + result.getName() 
                    + "' with status " + getStatusName(result.getStatus()) 
                    + " (Attempt " + retryCount + " of " + MAX_RETRY_COUNT + ")");
            return true;
        }
        return false;
    }

    private String getStatusName(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "SUCCESS";
            case ITestResult.FAILURE -> "FAILURE";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }
}
