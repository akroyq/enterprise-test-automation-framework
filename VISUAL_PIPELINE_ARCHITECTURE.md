# 🌟 Enterprise CI/CD & Dual-Environment Quality Architecture
### Interactive End-to-End Visual Flow: From Code Commit to QA & Production Sign-Off

> **Architecture At A Glance**: Look at the diagrams below to understand the complete lifecycle of how code moves from a developer's feature branch through automated QA verification on Render, and finally to verified Production deployment.

---

## 🗺️ Master Visual Architecture Map

```mermaid
flowchart TD
    %% Styling Definitions
    classDef dev fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#0369a1,rx:10,ry:10;
    classDef pr fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#92400e,rx:10,ry:10;
    classDef qa fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#166534,rx:10,ry:10;
    classDef prod fill:#f3e8ff,stroke:#9333ea,stroke-width:2px,color:#6b21a8,rx:10,ry:10;
    classDef failure fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#991b1b,rx:10,ry:10;
    classDef gate fill:#1e293b,stroke:#0f172a,stroke-width:2px,color:#ffffff,rx:10,ry:10;

    %% Lane 1: Development
    subgraph LANE1 ["💻 1. DEVELOPER WORKSPACE"]
        DEV_CODE["Feature Branch: feature/claim-fix<br/>• Writes Code in demo-app/ or automation/<br/>• Runs Local Pre-Flight: mvn test-compile"]:::dev
        PUSH_PR["git push & Open Pull Request<br/>(feature/claim-fix ➔ qa branch)"]:::dev
    end

    %% Lane 2: PR Gating
    subgraph LANE2 ["⚡ 2. PRE-MERGE QUALITY GATE (GitHub Actions CI)"]
        PR_BUILD["Stage 1: Build Verification<br/>• Java 17 compile across all 3 modules"]:::pr
        PR_EPHEMERAL["Stage 2 & 3: Fast Gate<br/>• Runs FastAPI in-runner (localhost:8000)<br/>• REST Assured API Suite (P0 Gate)"]:::pr
        PR_UI["Stage 4: Headless Selenium UI<br/>• Headless Chrome E2E Journeys"]:::pr
        PR_VERDICT{"Stage 5: Quality Gate<br/>100% Pass Rate?"}:::gate
        PR_BLOCKED["❌ MERGE BLOCKED<br/>Defect Caught Before Merge!"]:::failure
        PR_MERGE["✅ PR APPROVED<br/>Merge into 'qa' Allowed"]:::qa
    end

    %% Lane 3: Dedicated QA
    subgraph LANE3 ["🧪 3. DEDICATED QA ENVIRONMENT (Render.com)"]
        QA_DEPLOY["🚀 Auto-Deploy to QA Server<br/>Branch: 'qa'<br/>https://insurance-claims-demo.onrender.com"]:::qa
        QA_WAKE["Automatic Server Wake-up<br/>Polls /docs until 200 OK"]:::qa
        QA_SUITE["🔬 Live QA Regression Suite<br/>• REST Assured API Boundary Tests<br/>• Selenium Headless UI Journeys"]:::qa
        QA_REPORT["📊 ExtentReports 5.x HTML Published<br/>Retained 14 Days in CI Artifacts"]:::qa
        NIGHTLY_CRON["🌙 Nightly 00:00 UTC Cross-Browser Matrix<br/>Parallel Runs on Chrome & Firefox"]:::qa
    end

    %% Lane 4: Production
    subgraph LANE4 ["🚀 4. DEDICATED PRODUCTION ENVIRONMENT (Render.com)"]
        PROD_PR["Open PR: 'qa' ➔ 'main'<br/>(Release Candidate Sign-Off)"]:::prod
        PROD_TRIGGER["Trigger Production Release Pipeline<br/>• Push Tag: git tag v1.0.0 OR<br/>• 1-Click UI: release-production.yml"]:::prod
        PROD_GATE1["Stage 1: QA Pre-Flight Check<br/>Validates 100% Pass on QA first"]:::prod
        PROD_HUMAN_GATE{"🛑 HUMAN MANUAL APPROVAL<br/>QA Lead / Release Manager Sign-off<br/>Approve and Deploy?"}:::gate
        PROD_DEPLOY["Stage 2: Live Production Deploy<br/>Branch: 'main'<br/>https://insurance-claims-prod.onrender.com"]:::prod
        PROD_SMOKE["Stage 3: Live Smoke Sanity Gate<br/>• Critical API calculation check<br/>• Live Production homepage & journeys"]:::prod
        PROD_LIVE["🎉 PRODUCTION LIVE & VERIFIED!<br/>Zero-Defect Release Sign-Off (30-day Reports)"]:::prod
    end

    %% Flow Connections
    DEV_CODE --> PUSH_PR
    PUSH_PR --> PR_BUILD
    PR_BUILD --> PR_EPHEMERAL
    PR_EPHEMERAL --> PR_UI
    PR_UI --> PR_VERDICT

    PR_VERDICT -->|No / Regression| PR_BLOCKED
    PR_VERDICT -->|Yes / 0 Defects| PR_MERGE

    PR_MERGE -->|Merge to 'qa'| QA_DEPLOY
    QA_DEPLOY --> QA_WAKE
    QA_WAKE --> QA_SUITE
    QA_SUITE --> QA_REPORT
    QA_REPORT -.-> NIGHTLY_CRON

    QA_REPORT ==>|QA Sign-Off: Create PR| PROD_PR
    PROD_PR -->|Merge into 'main'| PROD_TRIGGER
    PROD_TRIGGER --> PROD_GATE1
    PROD_GATE1 --> PROD_HUMAN_GATE
    PROD_HUMAN_GATE -->|Approved by Human| PROD_DEPLOY
    PROD_HUMAN_GATE -.->|Rejected / Blocked| PROD_GATE1
    PROD_DEPLOY --> PROD_SMOKE
    PROD_SMOKE --> PROD_LIVE
```

---

## 🎬 Step-by-Step Sequence Animation (Lifecycle Diagram)

This sequence diagram illustrates the exact communication between **Developer, GitHub Actions, Render QA, and Render Production**:

```mermaid
sequenceDiagram
    autonumber
    actor Dev as 👨‍💻 Developer
    participant Git as 🐙 GitHub (Repo & PR)
    participant CI as ⚙️ GitHub Actions CI
    participant QA as 🧪 Render QA Server (Branch: qa)
    participant Prod as 🚀 Render Prod Server (Branch: main)
    actor Lead as 👔 QA Lead / Release Manager

    %% Step 1: Branch & PR to QA
    rect rgb(238, 246, 255)
    Note over Dev,CI: PHASE 1: Feature Branch & PR to 'qa' Quality Gate
    Dev->>Git: git push origin feature/claim-fix
    Dev->>Git: Open Pull Request (feature/claim-fix ➔ 'qa')
    Git->>CI: Trigger 'Enterprise Quality CI/CD' (PR to qa Event)
    CI->>CI: Compile Java 17 Parent & Modules
    CI->>CI: Start in-runner FastAPI (localhost:8000)
    CI->>CI: Run REST Assured API Tests
    CI->>CI: Run Headless Chrome UI Tests
    CI-->>Git: Post PR Summary: ✅ ALL GATES PASSED
    end

    %% Step 2: Merge to QA & Dedicated QA Deployment
    rect rgb(235, 253, 240)
    Note over Git,QA: PHASE 2: Merge into 'qa' & Auto-Deploy to QA Environment
    Dev->>Git: Click 'Merge Pull Request' into 'qa'
    Git->>QA: GitHub Webhook ➔ Trigger Auto-Deploy on branch 'qa'
    QA->>QA: pip install -r requirements.txt & uvicorn app:app
    Git->>CI: Trigger Post-Merge CI Pipeline on 'qa'
    CI->>QA: Active Ping https://insurance-claims-demo.onrender.com/docs
    QA-->>CI: 200 OK (QA Server Awake & Healthy)
    CI->>QA: Run Full REST Assured API Suite on Live QA
    CI->>QA: Run Selenium UI Headless Chrome on Live QA
    CI-->>Lead: ExtentReports 5.x HTML Generated (0 Defects)
    Lead-->>Dev: QA Sign-off Granted! Ready for Production.
    end

    %% Step 3: Promote to Main & Production Release
    rect rgb(250, 245, 255)
    Note over Lead,Prod: PHASE 3: Promote 'qa' ➔ 'main' & Production Release Gate
    Lead->>Git: Open Pull Request: 'qa' ➔ 'main'
    Lead->>Git: Merge PR into 'main'
    Lead->>CI: Trigger 'release-production.yml' (Workflow or Tag: v1.0.0)
    CI->>QA: Re-verify 100% P0 tests on QA (Pre-Flight)
    QA-->>CI: Confirmed 100% Pass
    CI->>Lead: Prompt for Human Manual Approval
    Lead->>CI: Type APPROVE_PRODUCTION_DEPLOYMENT
    CI->>Prod: Deploy Release Artifact to https://insurance-claims-prod.onrender.com
    Prod->>Prod: Start Production Service (Branch: main)
    CI->>Prod: Poll https://insurance-claims-prod.onrender.com/docs
    Prod-->>CI: 200 OK (Production Online)
    CI->>Prod: Execute Production Smoke Tests (testng-smoke.xml)
    Prod-->>CI: Smoke Tests 100% PASS
    CI-->>Lead: 🎉 Production Verified Sign-Off Archived (30 Days)
    end
```

---

## 🔄 Dynamic Enterprise State Machine (Ticket/Code Lifecycle Flow)

This state diagram visualizes the exact states, decision gates, and transitions every commit undergoes before reaching customers:

```mermaid
stateDiagram-v2
    direction TB

    [*] --> FeatureBranch: 💻 Developer creates branch 'feature/*' from 'qa'
    
    state FeatureBranch {
        [*] --> CodeDevelopment: Writes code in demo-app/ or automation/
        CodeDevelopment --> LocalPreFlight: Run local 'mvn test-compile -DskipTests'
        LocalPreFlight --> PushRemote: git push -u origin feature/*
    }

    PushRemote --> PullRequestQA: 🐙 Open Pull Request to 'qa'

    state PullRequestQA {
        [*] --> Stage1_Build: GitHub Actions 'ci.yml' Triggered
        Stage1_Build --> Stage2_FastAPI: In-runner spin-up (localhost:8000)
        Stage2_FastAPI --> Stage3_APIGate: REST Assured P0 Boundary Tests
        Stage3_APIGate --> Stage4_UIGate: Headless Chrome E2E Journeys
        Stage4_UIGate --> Stage5_GateVerdict: Quality Gate Evaluation
        
        Stage5_GateVerdict --> PR_Blocked: ❌ Any Test Failed (<100%)
        Stage5_GateVerdict --> PR_Approved: ✅ 100% Pass Rate
    }

    PR_Blocked --> CodeDevelopment: Fix bug on local feature branch
    PR_Approved --> MergeQA: 🔀 Click 'Merge pull request' into 'qa'

    state MergeQA {
        [*] --> RenderQAHook: Render Webhook Detects 'qa' Push
        RenderQAHook --> BuildQAService: pip install & uvicorn restart
        BuildQAService --> QALiveActive: 🧪 Deployed to insurance-claims-demo
    }

    MergeQA --> QAVerification: 🔬 Dedicated QA Verification

    state QAVerification {
        [*] --> PostMergeCI: 'ci.yml' runs against Live QA URL
        PostMergeCI --> ExtentReportsPublished: ExtentReports 5.x HTML Published (14d)
        ExtentReportsPublished --> NightlyMatrix: 🌙 00:00 UTC Chrome + Firefox
        NightlyMatrix --> QASignOffGranted: 👔 QA Lead Signs Off
    }

    QASignOffGranted --> PullRequestMain: 🚀 Open PR 'qa' ➔ 'main'

    state PullRequestMain {
        [*] --> MergeMain: PR Approved & Merged into 'main'
        MergeMain --> ProdTrigger: Trigger 'release-production.yml' (Tag / UI)
        ProdTrigger --> QA_PreFlightCheck: Re-verify 100% P0 on QA First
        QA_PreFlightCheck --> HumanApprovalGate: 🛑 Human Operator Confirmation
        HumanApprovalGate --> ProdDeployment: Deploy to insurance-claims-prod
        ProdDeployment --> LiveSmokeGate: Run testng-smoke.xml on Production
    }

    PullRequestMain --> ProductionVerifiedLive: 🎉 ZERO-DEFECT PRODUCTION LIVE
    ProductionVerifiedLive --> [*]
```

---

## ⚡ High-Velocity Code Promotion Pipeline (Live Path)

```
  ┌─────────────────┐       PR to 'qa'       ┌────────────────────────┐
  │ 💻 DEVELOPER    ├───────────────────────►│ ⚡ IN-RUNNER CI GATES   │
  │ feature/xyz     │                        │  • Build Verification  │
  └─────────────────┘                        │  • REST Assured API P0 │
                                             │  • Selenium Headless UI│
                                             └───────────┬────────────┘
                                                         │ 100% Pass
                                                         ▼
  ┌─────────────────────────────────┐   Auto-Deploy  ┌────────────────────────┐
  │ 🧪 DEDICATED QA ENVIRONMENT     │◄───────────────┤ 🐙 'qa' BRANCH MERGE   │
  │ insurance-claims-demo.onrender  │                └────────────────────────┘
  │  • ExtentReports 5.x HTML (14d) │
  │  • Nightly Matrix (Chrome+FF)   │
  └────────────────┬────────────────┘
                   │ QA Lead Sign-off
                   ▼
  ┌─────────────────────────────────┐   Merge & Gate ┌────────────────────────┐
  │ 🛑 PRODUCTION HUMAN GATE        ├───────────────►│ 🚀 'main' BRANCH MERGE │
  │ 'APPROVE_PRODUCTION_DEPLOYMENT' │                └───────────┬────────────┘
  └─────────────────────────────────┘                            │
                                                                 ▼
  ┌─────────────────────────────────┐   Live Smoke   ┌────────────────────────┐
  │ 🎉 PRODUCTION VERIFIED LIVE     │◄───────────────┤ 🌐 LIVE PROD DEPLOY    │
  │ insurance-claims-prod.onrender  │                │ insurance-claims-prod  │
  └─────────────────────────────────┘                └────────────────────────┘
```

---

## 📋 Environment & Pipeline Quick Reference Matrix

| Metric | 🧪 Dedicated QA Environment | 🚀 Dedicated Production Environment |
| :--- | :--- | :--- |
| **Tracked Git Branch** | `qa` | `main` |
| **Live Web URL** | [https://insurance-claims-demo.onrender.com](https://insurance-claims-demo.onrender.com) | [https://insurance-claims-prod.onrender.com](https://insurance-claims-prod.onrender.com) |
| **API Docs (Swagger)** | `/docs` | `/docs` |
| **Target Audience** | QA Engineers, Developers, Automation Suites | Real End Users, Live Insurance Claims |
| **Deploy Mechanism** | Continuous Auto-deploy on merge to `qa` branch | Controlled Release Tag (`v*.*.*`) / PR to `main` with Human Approval |
| **Active Pipelines** | `ci.yml` & `nightly-crossbrowser.yml` | `release-production.yml` |
| **Automated Test Scope**| 100% P0/P1 Calculation, Boundary & UI Regressions | Critical Path Smoke Tests (`testng-smoke.xml`) |
| **Report Retention** | 14 Days (ExtentReports HTML Spark) | 30 Days (Release Sign-off Dashboards) |

---

## 🎯 Key Takeaway for Architects & Interviewers

```
                ┌────────────────────────────────────────────────────────┐
                │          ENTERPRISE BRANCH & GATE HIERARCHY            │
                ├────────────────────────────────────────────────────────┤
                │ 1. feature/* ➔ PR to 'qa'   (In-Runner Test Gate)     │
                │ 2. Merge to 'qa'            (Auto-Deploy QA & Test)    │
                │ 3. PR 'qa' ➔ 'main'         (Human Approval Gate)      │
                │ 4. Deploy 'main' to Prod    (Live Smoke Sanity Gate)   │
                └────────────────────────────────────────────────────────┘
```
