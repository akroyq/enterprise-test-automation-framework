package com.qualityarchitect.ui.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ClaimsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "[aria-label='Policy ID']")
    private WebElement policy;

    @FindBy(css = "[aria-label='Product']")
    private WebElement product;

    @FindBy(css = "[aria-label='Claim amount']")
    private WebElement amount;

    @FindBy(css = "[aria-label='Deductible']")
    private WebElement deductible;

    @FindBy(xpath = "//button[normalize-space()='Submit claim']")
    private WebElement submit;

    @FindBy(xpath = "//button[normalize-space()='Calculate claim']")
    private WebElement calculate;

    @FindBy(css = "[data-testid='claim-status']")
    private WebElement status;

    @FindBy(css = "[data-testid='claim-id']")
    private WebElement claimId;

    @FindBy(css = "[data-testid='settlement']")
    private WebElement settlement;

    public ClaimsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public ClaimsPage open(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOf(policy));
        return this;
    }

    public ClaimsPage enterClaim(String policyId, String productName,
                                  String claimAmount, String deductibleValue) {
        wait.until(ExpectedConditions.visibilityOf(policy)).sendKeys(policyId);
        new Select(product).selectByValue(productName);
        amount.sendKeys(claimAmount);
        deductible.sendKeys(deductibleValue);
        return this;
    }

    public ClaimsPage submit() {
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
        wait.until(ExpectedConditions.textToBePresentInElement(status, "SUBMITTED"));
        return this;
    }

    public ClaimsPage calculate() {
        wait.until(ExpectedConditions.elementToBeClickable(calculate)).click();
        wait.until(ExpectedConditions.textToBePresentInElement(settlement, "900"));
        return this;
    }

    public String status() {
        return wait.until(ExpectedConditions.visibilityOf(status)).getText();
    }

    public String claimId() {
        return wait.until(ExpectedConditions.visibilityOf(claimId)).getText();
    }

    public String settlement() {
        return wait.until(ExpectedConditions.visibilityOf(settlement)).getText();
    }
}
