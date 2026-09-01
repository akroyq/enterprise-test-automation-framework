package com.qualityarchitect.api.tests;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.qualityarchitect.core.reporting.ExtentTestListener;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Listeners(ExtentTestListener.class)
public abstract class BaseApiTest {
    protected Configuration configuration;

    @BeforeClass(alwaysRun = true)
    public void configure() {
        RestAssured.baseURI =
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8000");
        configuration = Configuration.defaultConfiguration();
    }

    protected DocumentContext getPayloadContext(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Test data file not found: " + resourcePath);
            }
            String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return JsonPath.using(configuration).parse(jsonContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read test data from " + resourcePath, e);
        }
    }
}
