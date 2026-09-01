# Engineering Refinement Log (Human-in-the-Loop Interventions)

**Role:** Senior Quality Architect / Lead SDET  
**Purpose:** This log details the technical observations, root causes, and architectural corrections applied during the development and stabilization of the test suites and demo platform.

---

### Intervention 1: Fixing Backend Endpoint 404 & Float Type Cast Error
- **Observation during initial `mvn test` execution**:
  - The API test suite failed on `assessorIntegration_shouldAcceptValidPayload` with `404 Not Found` and `calculation_shouldBeIdempotent` with:
    `java.lang.ClassCastException: class java.lang.Float cannot be cast to class java.lang.Integer`.
- **Root Cause**:
  1. The demo service lacked the `/integrations/assessor` route expected by third-party webhook contracts.
  2. Python's arithmetic computation in FastAPI returned float (`900.0`) which REST Assured deserialized as `Float`, causing a class cast mismatch against TestNG's integer assertions.
- **Architectural Fix**:
  - Added the `@app.post("/integrations/assessor", status_code=202)` endpoint in `demo-app/app.py` returning `{ "accepted": True, "correlation_id": "CORR-..." }`.
  - Normalized the settlement return value to clean integers when whole numbers are calculated.

---

### Intervention 2: Upgrading UI Automation to PageFactory `@FindBy` Pattern
- **Observation**:
  - `ClaimsPage.java` was initially using raw `By` selectors with direct `driver.findElement()` calls scattered across methods.
- **Root Cause / Architecture Debt**:
  - Raw `By` lookups lack centralized element encapsulation, making maintenance cumbersome as UI screens grow across 8 Scrum teams.
- **Architectural Fix**:
  - Refactored `ClaimsPage.java` to use the standard Selenium `@FindBy` annotations with `PageFactory.initElements(driver, this)`.
  - Kept explicit `WebDriverWait` synchronization on visible web elements to guarantee zero test flakiness.

---

### Intervention 3: Decoupling Test Data with Jayway JsonPath (`DocumentContext`)
- **Observation**:
  - Test payloads were originally hardcoded strings directly inside Java test methods.
- **Root Cause / Architecture Debt**:
  - Hardcoded JSON strings in Java code become unmaintainable when schemas evolve and make negative/boundary testing verbose.
- **Architectural Fix**:
  - Added `com.jayway.jsonpath:json-path:2.9.0` to `pom.xml`.
  - Moved JSON templates into `src/test/resources/testdata/createClaim.json` and `assessorIntegration.json`.
  - Implemented `getPayloadContext(path)` in `BaseApiTest` so tests dynamically modify values using `requestBody.set("$.claim_amount", val)` or delete keys for negative testing (`requestBody.delete("$.policy_id")`).

---

### Intervention 4: Standardizing Java CamelCase Method Naming
- **Observation**:
  - Several test methods used underscores in their names (e.g. `createClaim_shouldReturn201`, `crossFunctionalFlow_createThenCalculate_shouldShowSettlement`).
- **Architectural Fix**:
  - Renamed all test methods across `ClaimsApiTest.java` and `ClaimsUiTest.java` to standard Java `camelCase` conventions (e.g. `createClaimShouldReturn201`, `crossFunctionalFlowCreateThenCalculateShouldShowSettlement`).

---

### Intervention 5: UI Form Layout, Label Spacing & Accessibility Attributes
- **Observation**:
  - The initial web page had unstyled HTML with cramped and overlapping form fields.
- **Architectural Fix**:
  - Built a clean, modern CSS card layout with individual `.form-group` containers providing proper margin and vertical hierarchy between labels and textboxes.
  - Ensured all required `aria-label` and `data-testid` attributes were preserved so Selenium locators remain rock-solid.

---

### Intervention 6: Enterprise Business Reporting with ExtentReports 5.x
- **Observation**:
  - Default TestNG HTML reports lacked executive visual metrics and failure screenshot artifacts for business stakeholders.
- **Architectural Fix**:
  - Integrated ExtentReports 5.1.2 Spark reporter with thread-safe `ThreadLocal<ExtentTest>`.
  - Built automatic Base64 failure screenshot capturing in `ExtentTestListener` without disk bloat.

---

### Intervention 7: Thread-Safe Parallel Execution & Flaky Test Retry
- **Observation**:
  - Sequential test execution slowed down CI feedback times, and shared WebDriver instances caused race conditions when parallelized.
- **Architectural Fix**:
  - Refactored `BaseUiTest` to isolate browser sessions using `ThreadLocal<WebDriver>`.
  - Implemented dynamic `IRetryAnalyzer` + `IAnnotationTransformer` auto-retrying transient failures up to 2 times without manual annotations.

---

### Intervention 8: Multi-Module Maven Refactoring (`automation-core`)
- **Observation**:
  - Reporting, retry analyzers, and database/email/data-generator utilities were duplicated across `api-restassured` and `ui-selenium`.
- **Architectural Fix**:
  - Created a shared `automation-core` library under parent aggregator POM (`claims-automation-parent`).
  - Centralized generic reporting, retry, JDBC `DatabaseUtil`, `EmailUtil`, and `DataGeneratorUtil` in `com.qualityarchitect.core.*`, eliminating 100% duplicate code.

---

### Intervention 9: Dynamic Multi-Browser DriverFactory (Chrome, Edge, Firefox)
- **Observation**:
  - The UI test suite only supported Google Chrome, limiting cross-browser compatibility verification.
- **Architectural Fix**:
  - Built `DriverFactory` supporting Chrome, Microsoft Edge, and Mozilla Firefox with automated driver resolution via Selenium Manager.
  - Added dedicated `testng-crossbrowser.xml` suite runner for concurrent cross-browser testing.

