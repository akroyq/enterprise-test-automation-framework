package com.qualityarchitect.core.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

public class ExtentReportManager {
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    private static final String REPORT_DIR = "target/extent-reports";
    private static final String REPORT_FILE_PATH = REPORT_DIR + "/ExtentReport.html";

    private ExtentReportManager() {
        // Utility class
    }

    public static synchronized ExtentReports getInstance() {
        return getInstance("Insurance Claims Test Automation Report");
    }

    public static synchronized ExtentReports getInstance(String suiteName) {
        if (extentReports == null) {
            File reportDir = new File(REPORT_DIR);
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_FILE_PATH);
            sparkReporter.config().setDocumentTitle(suiteName);
            sparkReporter.config().setReportName(suiteName);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
            sparkReporter.config().setEncoding("UTF-8");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("Application", "Insurance Claims Processing Platform");
            extentReports.setSystemInfo("Suite Name", suiteName);
            extentReports.setSystemInfo("Environment", System.getenv().getOrDefault("ENV", "QA / Local"));
            extentReports.setSystemInfo("Base URL", System.getenv().getOrDefault("BASE_URL", "http://localhost:8000"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Operating System", System.getProperty("os.name"));
            extentReports.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extentReports;
    }

    public static void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }

    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    public static void removeTest() {
        extentTestThreadLocal.remove();
    }

    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
