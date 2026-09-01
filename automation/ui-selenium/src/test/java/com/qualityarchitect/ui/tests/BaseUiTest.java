package com.qualityarchitect.ui.tests;

import com.qualityarchitect.core.reporting.ExtentTestListener;
import com.qualityarchitect.ui.driver.DriverFactory;
import com.qualityarchitect.ui.driver.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@Listeners(ExtentTestListener.class)
public abstract class BaseUiTest implements WebDriverProvider {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional String browser) {
        WebDriver currentDriver = DriverFactory.createDriver(browser);
        driverThreadLocal.set(currentDriver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver currentDriver = driverThreadLocal.get();
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    @Override
    public WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    protected String baseUrl() {
        return System.getenv().getOrDefault("BASE_URL", "http://localhost:8000");
    }
}
