package com.qualityarchitect.ui.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    private DriverFactory() {
        // Factory class
    }

    public static WebDriver createDriver(String browserName) {
        String browser = (browserName != null && !browserName.isBlank()) 
                ? browserName.trim().toLowerCase() 
                : System.getProperty("browser", System.getenv().getOrDefault("BROWSER", "chrome")).trim().toLowerCase();

        boolean headless = !"false".equalsIgnoreCase(
                System.getProperty("headless", System.getenv().getOrDefault("HEADLESS", "true"))
        );

        return switch (browser) {
            case "firefox", "ff" -> createFirefoxDriver(headless);
            case "edge", "msedge" -> createEdgeDriver(headless);
            case "chrome" -> createChromeDriver(headless);
            default -> {
                System.out.println("[DriverFactory] Warning: Unsupported browser '" + browser + "'. Defaulting to Chrome.");
                yield createChromeDriver(headless);
            }
        };
    }

    private static WebDriver createChromeDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        return new ChromeDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        return new EdgeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1440");
        options.addArguments("--height=1000");
        return new FirefoxDriver(options);
    }
}
