# AI Agent Prompt Catalog — Insurance Claims QA Engineering

This document outlines the structured prompt chains used with the AI coding assistant to ingest domain requirements, quantify risks, generate test cases, and synthesize automated test code.

---

### Prompt 1: Requirements Ingestion & Risk Modeling
```text
Act as a Principal QA Architect. Analyze the following insurance claims platform problem statement:
- 8 Scrum teams (3 Product, 3 Platform, 2 Integrations).
- 50% of production incidents stem from Claims Calculation errors and Third-Party Integration failures.
- Releases are bi-weekly with quarterly major releases.

Tasks:
1. Define a quantitative risk assessment matrix (Risk = Impact × Likelihood × Volatility × Integration Complexity).
2. Group features into P0 (Critical/Blocker), P1 (High), and P2 (Medium) priorities.
3. Establish unambiguous Release Quality Gates for continuous deployment.
```

---

### Prompt 2: API Test Scenario & Automation Generation (Java + REST Assured)
```text
Act as a Senior Test Automation Architect. Based on the claims processing OpenAPI specification:
Endpoints:
- POST /claims (Create claim)
- GET /claims/{id} (Retrieve claim)
- POST /claims/{id}/calculate (Calculate settlement)
- POST /integrations/assessor (Ingest 3rd party assessment)

Generate a production-ready Java 17 + REST Assured + TestNG test suite:
1. Separate JSON payload templates into 'src/test/resources/testdata/' files.
2. Use Jayway JsonPath DocumentContext to read and dynamically modify payload fields (e.g. set values, delete mandatory fields for negative tests).
3. Implement parameterized DataProviders for boundary calculation values (Amount > Deductible, Amount == Deductible, Amount < Deductible).
4. Verify idempotency and appropriate HTTP status codes (201, 202, 200, 400/422, 404).
5. Adhere to standard Java camelCase method naming.
```

---

### Prompt 3: UI Automation Generation (Java + Selenium + PageFactory)
```text
Act as a Lead SDET. Design a robust Page Object Model (POM) for the Insurance Claims web UI using Selenium WebDriver 4 and Java 17.

Requirements:
1. Use Selenium @FindBy annotations with PageFactory.initElements(driver, this) for element encapsulation.
2. Use explicit WebDriverWait with ExpectedConditions (no hardcoded Thread.sleep).
3. Implement fluent-style method chaining (open -> enterClaim -> submit -> calculate).
4. Validate status badge transitions (SUBMITTED) and settlement computation outputs.
5. Adhere to standard Java naming standards.
```

---

### Prompt 4: Defect Diagnosis & Automated Code Refinement
```text
Analyze the following TestNG execution failure log where REST Assured threw:
"ClassCastException: java.lang.Float cannot be cast to java.lang.Integer"
and HTTP 404 on "/integrations/assessor".

Tasks:
1. Identify the root cause in the backend FastAPI application and the test expectations.
2. Provide corrective code changes in both the backend application and the test assertions.
3. Verify that all test cases execute cleanly with 100% pass rate.
```
