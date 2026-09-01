# Java Automation Architecture (Multi-Module Maven)

## 📦 Multi-Module & Standardized Package Structure
```
automation/
├── pom.xml                                  (Parent Aggregator)
├── automation-core/                         (Core Framework Library)
│   └── src/main/java/com/qualityarchitect/core/
│       ├── reporting/                       (ExtentReportManager, ExtentTestListener)
│       ├── retry/                           (RetryAnalyzer, AnnotationTransformer)
│       └── utils/                           (DatabaseUtil, EmailUtil, DataGeneratorUtil)
├── api-restassured/                         (API Automation Module)
│   └── src/test/java/com/qualityarchitect/api/
│       └── tests/                           (BaseApiTest, ClaimsApiTest)
└── ui-selenium/                             (UI Automation Module)
    └── src/test/java/com/qualityarchitect/ui/
        ├── driver/                          (WebDriverProvider)
        ├── pages/                           (ClaimsPage - Page Object Model)
        ├── utils/                           (ScreenshotUtil)
        └── tests/                           (BaseUiTest, ClaimsUiTest)
```




## 🌐 Multi-Browser & Cross-Browser Execution
The UI framework supports dynamic browser instantiation via [DriverFactory](file:///c:/Users/User/Downloads/Insurance_Claims_Quality_Architect_Submission_Final_Java_RESTAssured_Selenium_TestNG/submission/automation/ui-selenium/src/test/java/com/qualityarchitect/ui/driver/DriverFactory.java):
- Supported Browsers: **Chrome**, **Edge**, **Firefox**.
- Configurable via CLI property (`-Dbrowser=edge`), environment variable (`BROWSER=firefox`), or TestNG XML parameter.

```powershell
# Run with Chrome (default)
mvn test -pl ui-selenium -am

# Run with Microsoft Edge
mvn test -pl ui-selenium -am -Dbrowser=edge

# Run with Mozilla Firefox
mvn test -pl ui-selenium -am -Dbrowser=firefox

# Run dedicated cross-browser suite (Chrome + Edge + Firefox parallel)
mvn test -pl ui-selenium -am -Dsurefire.suiteXmlFiles=src/test/resources/testng-crossbrowser.xml
```

## Parallel Execution & Concurrency
- Configured in `testng.xml` using `parallel="methods"`.
- `api-restassured`: runs 3 concurrent test threads.
- `ui-selenium`: runs 2 concurrent browser sessions isolated with `ThreadLocal<WebDriver>`.

## Automatic Flaky Test Retry
- Powered by `RetryAnalyzer` (`IRetryAnalyzer`) and dynamic `AnnotationTransformer` (`IAnnotationTransformer`).
- Automatically retries failing tests up to `2` times (configurable via `-DmaxRetryCount=N` or `MAX_RETRY_COUNT` environment variable).

## Test Groups & Risk-Based Execution
Test cases are grouped by lifecycle (`smoke`, `regression`), risk priority (`p0`, `p1`, `p2`), and domain (`claim-creation`, `calculation`, `integration`, `e2e`):
```powershell
# Run smoke tests only
mvn test -Dgroups="smoke"

# Run P0 critical path tests only
mvn test -Dgroups="p0"

# Run full regression suite
mvn test
```

## 🛠️ Enterprise QA Utilities
1. **`DatabaseUtil`**:
   - Executes SQL queries (`executeQuery`, `getSingleValue`, `executeUpdate`, `executePreparedStatement`).
   - Configurable via `DB_URL`, `DB_USER`, `DB_PASSWORD`.
2. **`EmailUtil`**:
   - Sends test execution reports with HTML metric summaries and ExtentReport HTML attachment.
   - Configurable via `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` (includes graceful simulation fallback for local runs).
3. **`DataGeneratorUtil`**:
   - Generates realistic test data: policy IDs (`POL-xxxx`), claim amounts, deductibles, policy types, full names, emails, phone numbers, and correlation UUIDs.

## Reporting
Both test suites generate rich HTML ExtentReports at:
- `automation/api-restassured/target/extent-reports/ExtentReport.html`
- `automation/ui-selenium/target/extent-reports/ExtentReport.html`

Features:
- Executive summary with visual test metrics & pass rate charts.
- Category filtering by TestNG Groups (`smoke`, `regression`, `p0`, etc.).
- Test duration, timestamps, environment and system metadata.
- Automatic failure screenshot capture embedded in UI test reports.
- Thread-safe execution during parallel test runs.


