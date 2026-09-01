# Test Strategy & Quality Engineering Roadmap
**Author:** Amit Kumar Roy 
**Domain:** Insurance Claims Processing (Auto, Health, Property)  
**Target:** 8 Distributed Scrum Teams | Bi-weekly Releases  

---

## 1. Context & The Ground Reality

When analyzing our claims processing platform, a few critical patterns immediately stand out:
- We have **8 Scrum teams** (3 building product lines, 3 managing core platform services, and 2 handling third-party integrations) with a 2 Dev : 1 QA ratio.
- **50% of our production incidents** are concentrated in just two areas: **Claims Calculation Errors** and **Third-Party Assessor Integration Failures**.
- Platform services had existing automation, but product modules and third-party webhooks were largely verified manually with unknown regression coverage.
- Because all features were treated equally in sprint cycles, QAs spent excessive time manually clicking low-risk UI screens while high-risk calculation boundaries slipped into production.

---

## 2. Root Cause Breakdown (Why Bugs Escaped)

| Problem Area | Root Cause Identified | Engineering Fix |
|---|---|---|
| **Settlement Calculation Bugs** | Floating-point rounding mismatches and missing boundary tests ($Amount \le Deductible$) in sprint regression. | Implemented parameterized automated boundary tests covering $Amount > Deductible$, $Amount == Deductible$, and $Amount < Deductible$. |
| **3rd-Party Assessor Failures** | Asynchronous payload contract drift without contract verification or correlation tracking. | Introduced integration contract testing with strict correlation ID and status code validation (`202 Accepted`). |
| **Testing Bottlenecks** | Manual regression took 2+ days before each release because test data was hardcoded. | Decoupled test data into external JSON files and automated P0/P1 flows in CI. |

---

## 3. Practical Risk-Based Testing Model

Rather than trying to automate 100% of everything at once, we prioritize testing effort using an objective mathematical risk score:

$$\text{Risk Score} = \text{Impact} \times \text{Likelihood} \times \text{Volatility} \times \text{Integration Complexity}$$

- **Impact (1–5)**: Direct financial loss or regulatory non-compliance (e.g. wrong payouts = 5).
- **Likelihood (1–5)**: Complex business rules, edge cases, and arithmetic operations (e.g. deductible deduction = 4).
- **Volatility (1–5)**: Frequency of code churn and requirement changes across sprints.
- **Integration Complexity (1–5)**: External dependencies, 3rd party webhooks, payload schemas.

### Priority Tiers & How We Test Them:

```
[ P0 - Critical ] -> 100% Automated in CI (Blocking Gate)
                      • Settlement calculation engine
                      • Deductible floor limits
                      • Third-party assessor webhook ingestion

[ P1 - High ]     -> Automated Smoke + API Component Tests
                      • Multi-product claim creation (Auto, Health, Property)
                      • Mandatory field validations & schema errors
                      • UI state transitions (SUBMITTED)

[ P2 - Medium ]   -> UI Smoke + Exploratory Testing in Sprints
                      • Claim filtering & history queries
                      • Layout responsiveness & visual validation
```

---

## 4. Automation Framework Architecture

We chose a battle-tested **Java 17 Multi-Module Maven ecosystem** designed for multi-team maintainability and enterprise scalability:

```
                          ┌────────────────────────────┐
                          │   GitHub Actions Pipeline  │
                          │   (Automated PR & Push CI) │
                          └──────────────┬─────────────┘
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 ▼                       ▼                       ▼
     ┌───────────────────────┐ ┌───────────────────┐ ┌───────────────────────┐
     │  claims-api-restassured│ │claims-automation- │ │  claims-ui-selenium   │
     │  (REST Assured +      │ │core               │ │  (Selenium WebDriver  │
     │   JsonPath + TestNG)  │ │(Reporting, Retry, │ │   4 + Multi-Browser)  │
     └───────────┬───────────┘ │ Database, Email)  │ └───────────┬───────────┘
                 │             └─────────┬─────────┘             │
                 ▼                       ▼                       ▼
     ┌───────────────────────┐ ┌───────────────────┐ ┌───────────────────────┐
     │ External JSON Data    │ │ ExtentReports 5.x │ │ Page Object Model     │
     │ (src/test/resources/  │ │ Interactive HTML  │ │ with @FindBy &        │
     │  testdata/*.json)     │ │ Spark Dashboard   │ │ DriverFactory         │
     └───────────────────────┘ └───────────────────┘ └───────────────────────┘
```

### Key Architectural Choices:
1. **Multi-Module Separation of Concerns (`automation-core`)**:
   Core utilities (ExtentReports 5.x reporting manager, TestNG listeners, automatic retry transformers, JDBC database helper, email dispatcher, and synthetic data generators) are encapsulated in `automation-core`, eliminating duplicate code across API and UI suites.
2. **Dynamic JsonPath (`DocumentContext`)**:
   Instead of messy string concatenation or fragile Java POJOs for every request variation, we load clean JSON templates from `src/test/resources/testdata/` and use JsonPath to modify or delete specific keys on-the-fly (`requestBody.set()`, `requestBody.delete()`).
3. **Multi-Browser `DriverFactory` & Page Object Model**:
   Seamless cross-browser execution across **Google Chrome**, **Microsoft Edge**, and **Mozilla Firefox** using `DriverFactory`. Page objects encapsulate elements using `@FindBy` with zero static sleeps in favor of dynamic `WebDriverWait` conditions.
4. **Thread-Safe Concurrency & Isolated Sessions**:
   Parallel execution enabled (`parallel="methods"` / `parallel="tests"`) utilizing `ThreadLocal<WebDriver>` and `ThreadLocal<ExtentTest>` for race-condition-free test runs.
5. **Dynamic Flaky Test Retry**:
   TestNG `IRetryAnalyzer` paired with `IAnnotationTransformer` auto-retries transient network or timing failures without requiring manual method annotations.
6. **Executive Business Reporting (ExtentReports 5.x)**:
   Rich visual dashboards with test execution duration, environment metadata, group category filtering (`smoke`, `regression`, `p0`), and embedded Base64 failure screenshots.
7. **Java Naming Standards**:
   All test methods follow standard descriptive `camelCase` naming (e.g. `createClaimShouldReturn201`, `calculationShouldMatchReferenceData`).


---

## 5. Release Quality Gates (Enforced via CI/CD)

To ensure zero calculation or integration defects reach production:

1. **Pull Request Gate**:
   - `mvn test` in `api-restassured` must pass 100%.
   - No regression in P0 calculation boundary tests.
2. **Pre-Deployment / Nightly Gate**:
   - Headless Selenium UI regression suite must pass on staging.
   - 0 unresolved P0/P1 bugs.

---

## 6. Engineering Metrics We Track

We focus on actionable engineering health metrics:
- **Defect Escape Rate (DER)**: Target $< 3\%$ of bugs reported post-release.
- **P0 Automation Coverage**: $100\%$ of all calculation and third-party integration test cases must be automated.
- **Flakiness Index**: Zero intermittent test failures across 50 consecutive CI runs.
- **CI Feedback Time**: Full API suite runs in $< 20$ seconds, allowing rapid developer feedback on every commit.
