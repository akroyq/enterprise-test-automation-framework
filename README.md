# Enterprise Insurance Claims Platform — Quality Architect Submission

This repository contains the end-to-end Quality Architecture, Risk-Based Test Strategy, AI Agent Artifacts, and Automated Test Suites for an enterprise multi-product Insurance Claims Processing Platform.

---

## 🏛️ Technology Stack & Architecture

- **Automation Architecture**: **Multi-Module Maven (`claims-automation-parent`)**
- **Core Framework (`automation-core`)**: **ExtentReports 5.x + TestNG Listeners + Dynamic Retry (`IRetryAnalyzer`) + QA Utilities (`DatabaseUtil`, `EmailUtil`, `DataGeneratorUtil`)**
- **API Automation (`api-restassured`)**: **Java 17 + REST Assured + TestNG + Jayway JsonPath + ExtentReports 5.x + Parallel Execution + Maven**
- **UI Automation (`ui-selenium`)**: **Java 17 + Selenium WebDriver 4 + Multi-Browser (`DriverFactory` for Chrome/Edge/Firefox) + TestNG + PageFactory (`@FindBy`) + ExtentReports 5.x + Parallel Execution (`ThreadLocal<WebDriver>`) + Maven**
- **Test Reporting**: **ExtentReports 5.x Spark Interactive HTML Reports (with group categorization and embedded failure screenshots for UI)**
- **Test Execution Resilience**: **Automatic Flaky Test Retry (`IRetryAnalyzer` + `IAnnotationTransformer`) & Multi-Threaded Parallel Test Runs**
- **Underlying Demo Application**: **FastAPI + Uvicorn (Python 3.10+)**
- **Continuous Integration (CI)**: **GitHub Actions** (`.github/workflows/ci.yml`)

---

## 📁 Repository Structure

```
submission/
├── CASE_STUDY.md           # Enterprise Risk-Based Strategy & Quality Architecture
├── STRATEGY_DOCUMENT.md    # Master Risk-Driven Quality Architecture Strategy
├── CICD_PIPELINE_GUIDE.md  # Standard Operating Procedure & CI/CD Architecture Guide
├── VISUAL_PIPELINE_ARCHITECTURE.md # Master Visual Flowchart & Animated Lifecycle Diagrams
├── README.md               # Quick start guide & architectural overview
├── .github/
│   └── workflows/
│       ├── ci.yml                   # GitHub Actions multi-stage automated pipeline
│       ├── nightly-crossbrowser.yml # Scheduled cross-browser regression pipeline
│       └── release-production.yml   # Production release & post-deploy smoke sanity gate
├── agents/                 # AI Agent Artifacts (Section 5B Deliverables)
│   ├── prompts.md          # Structured prompt engineering catalog
│   ├── system-instructions.md # Quality Architect persona & constraints
│   └── refinement-log.md   # Iterative human-in-the-loop refinement log
├── test-cases/             # Comprehensive Manual Test Specifications
│   ├── ui-manual-tests.md  # Detailed UI manual test matrix
│   └── api-manual-tests.md # Detailed API test cases with payloads & assertions
├── metrics/
│   └── coverage-matrix.csv # Requirements-to-Test Traceability Matrix
├── demo-app/               # Application Under Test
│   ├── app.py              # FastAPI service with UI and REST endpoints
│   └── requirements.txt    # Python dependencies
├── automation/             # Multi-Module Maven Automation Projects
│   ├── pom.xml             # Parent Aggregator POM (claims-automation-parent)
│   ├── automation-core/    # Core Framework Library (claims-automation-core)
│   │   ├── pom.xml
│   │   └── src/main/java/com/qualityarchitect/core/
│   │       ├── reporting/  (ExtentReportManager, ExtentTestListener)
│   │       ├── retry/      (RetryAnalyzer, AnnotationTransformer)
│   │       └── utils/      (DatabaseUtil, EmailUtil, DataGeneratorUtil)
│   ├── api-restassured/    # REST Assured API test suite with JsonPath
│   │   ├── pom.xml
│   │   └── src/test/
│   │       ├── resources/  (testng.xml, testng-smoke.xml, testdata/)
│   │       └── java/com/qualityarchitect/api/tests/ (BaseApiTest, ClaimsApiTest)
│   └── ui-selenium/        # Selenium WebDriver UI test suite (PageFactory + DriverFactory)
│       ├── pom.xml
│       └── src/test/
│           ├── resources/  (testng.xml, testng-smoke.xml, testng-crossbrowser.xml)
│           └── java/com/qualityarchitect/ui/
│               ├── driver/ (DriverFactory, WebDriverProvider)
│               ├── pages/  (ClaimsPage)
│               ├── utils/  (ScreenshotUtil)
│               └── tests/  (BaseUiTest, ClaimsUiTest)
└── video/
    └── README.md           # Demo walkthrough recording details
```

---

## 🚀 Quick Start & Test Execution

### 1. Start the Demo Application
```powershell
cd demo-app
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
Open **`http://localhost:8000`** in your browser to access the claims portal.

---

### 2. Execute API Automation Suite (REST Assured)
In a new terminal window:
```powershell
cd automation/api-restassured
mvn test
```

---

### 3. Execute UI Automation Suite (Selenium)
In a new terminal window:
```powershell
cd automation/ui-selenium
mvn test
```

> **Note**: Selenium Manager automatically provisions the appropriate browser driver. To run tests against the dedicated live environments, set the `BASE_URL` environment variable:
> - **QA Environment**: `$env:BASE_URL="https://insurance-claims-demo.onrender.com"`
> - **Production Environment**: `$env:BASE_URL="https://insurance-claims-prod.onrender.com"`
> - **Local Development**: Default fallback is `http://localhost:8000`.

---

### 4. 📈 Business & Stakeholder Test Reports (ExtentReports 5.x)
After test execution, interactive and business-friendly HTML reports with metrics, pass/fail statuses, parameters, and failure screenshots are available at:

- **API Test Report**: `automation/api-restassured/target/extent-reports/ExtentReport.html`
- **UI Test Report**: `automation/ui-selenium/target/extent-reports/ExtentReport.html`

To open the report in your browser (Windows PowerShell):
```powershell
# Open API Report
Start-Process automation/api-restassured/target/extent-reports/ExtentReport.html

# Open UI Report
Start-Process automation/ui-selenium/target/extent-reports/ExtentReport.html
```

---

### 5. 🔄 Continuous Integration & Quality Gates (GitHub Actions)
The repository enforces Quality Gates across automated GitHub Actions workflows:

1. **Pull Request & Main Branch CI (`.github/workflows/ci.yml`)**:
   - **Stage 1 (Build & Verification)**: Java 17 compile across all parent & child modules with Maven dependency caching.
   - **Stage 2 (Service Spin-up & Health Check)**: FastAPI claims backend launched with non-blocking polling health checks on `http://127.0.0.1:8000/docs`.
   - **Stage 3 (API REST Assured Gate)**: 100% calculation and boundary tests executed. **Blocking Gate**: 0 defects allowed.
   - **Stage 4 (UI Headless Gate)**: Selenium WebDriver 4 headless Chrome journeys executed with full screenshot capture on failure.
   - **Stage 5 (Executive Dispatch & Retention)**: ExtentReports 5.x Spark HTML reports, Surefire XMLs, and screenshots retained for **14 days** as CI build artifacts; automated execution summary published to `$GITHUB_STEP_SUMMARY`.

2. **Nightly Cross-Browser Regression (`.github/workflows/nightly-crossbrowser.yml`)**:
   - Scheduled nightly at `00:00 UTC` with on-demand `workflow_dispatch` trigger.
   - Matrix execution across **Chrome** and **Firefox** in parallel using `testng-crossbrowser.xml`.
   - Automated cross-browser artifact collection.

3. **Production Release & Deployment Gate (`.github/workflows/release-production.yml`)**:
   - Triggers on release version tags (`v*.*.*`) or manual promotion by Release Manager.
   - **Stage 1 (QA Sign-off)**: Validates 100% P0 regression pass rate on QA before deployment.
   - **Stage 2 (Deployment)**: Deploys immutable release artifact to live Production server.
   - **Stage 3 (Smoke Sanity Gate)**: Executes live API & UI Smoke suites against Production; generates 30-day retained release sign-off reports.

---

## 📊 Key Highlights for Test Architect Evaluation

1. **Risk-Driven Prioritization**: Mathematical risk modeling prioritizing P0 calculation engine and third-party integrations (responsible for 50% of prod incidents).
2. **Dynamic JSON Payload Handling**: Separation of test data templates using Jayway JsonPath `DocumentContext` for positive, boundary, and negative testing without hardcoded strings.
3. **PageFactory & Explicit Synchronization**: Modern POM pattern using `@FindBy` annotations and dynamic `WebDriverWait` explicit conditions.
4. **Thread-Safe Parallel Execution**: Multi-threaded test runs (`parallel="methods"`) with `ThreadLocal<WebDriver>` session isolation.
5. **Dynamic Flaky Test Retry**: Zero-configuration retry mechanism powered by `IRetryAnalyzer` + `IAnnotationTransformer`.
6. **Executive Business Reporting**: Interactive ExtentReports 5.x with metrics and embedded UI failure screenshots.
7. **Clean Code & Conventions**: 100% adherence to standard Java `camelCase` method conventions and multi-stage CI automation.
