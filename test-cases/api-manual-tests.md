# API Manual Test Case Specifications

**System:** Insurance Claims Core Microservices & Integration APIs  
**Reviewed By:** QA Lead / Quality Architect  
**Scope:** REST endpoints for Claims Lifecycle (Creation, Search, Calculation) and Third-Party Webhook Integrations.

---

### Test Cases Summary Table

| Test Case ID | Endpoint & Method | Priority | Objective / Purpose | Request Body / Parameters | Expected Status | Response Body & Assertion Checkpoints | Automation Status |
|---|---|---|---|---|---|---|---|
| **TC-API-001** | `POST /claims` | **P1** | Verify successful claim creation with valid payload | `{"policy_id":"POL-1001", "product":"auto", "claim_amount":1000, "deductible":100}` | `201 Created` | • `id` generated matching pattern `CLM-[A-F0-9]{10}`.<br/>• `status` is `SUBMITTED`.<br/>• `policy_id` equals `POL-1001`. | **Automated** (`ClaimsApiTest`) |
| **TC-API-002** | `POST /claims` | **P1** | Negative check: Missing mandatory `policy_id` | `{"product":"auto", "claim_amount":1000, "deductible":100}` | `422 Unprocessable Entity` (or `400`) | • Response indicates validation error on missing `policy_id`. | **Automated** (`ClaimsApiTest`) |
| **TC-API-003** | `POST /claims` | **P1** | Negative check: Negative claim amount | `{"policy_id":"POL-1001", "product":"auto", "claim_amount":-200, "deductible":100}` | `422 / 400` | • Response indicates `claim_amount` must be greater than or equal to 0. | **Manual** |
| **TC-API-004** | `POST /claims` | **P1** | Negative check: Empty string policy ID | `{"policy_id":"", "product":"auto", "claim_amount":1000, "deductible":100}` | `422 / 400` | • Response rejects payload due to minimum length constraint ($\ge 1$). | **Manual** |
| **TC-API-005** | `GET /claims/{id}` | **P1** | Fetch claim details using a valid Claim ID | Path param: `id = CLM-XXXXXXXXXX` | `200 OK` | • Returns complete JSON object with matching `id`, `policy_id`, `product`, `claim_amount`, and `deductible`. | **Manual** |
| **TC-API-006** | `GET /claims/{id}` | **P1** | Fetch claim with non-existent Claim ID | Path param: `id = CLM-INVALID-999` | `404 Not Found` | • `{"detail": "Claim not found"}` | **Manual** |
| **TC-API-007** | `GET /claims` | **P2** | List all registered claims | None | `200 OK` | • `{"items": [...], "count": total_count}` | **Manual** |
| **TC-API-008** | `GET /claims` | **P2** | Filter claims list by `policy_id` query parameter | Query param: `?policy_id=POL-1001` | `200 OK` | • All returned items in array have `policy_id == "POL-1001"`.<br/>• `count` equals length of `items`. | **Manual** |
| **TC-API-009** | `POST /claims/{id}/calculate` | **P0** | Standard settlement calculation ($Amount > Deductible$) | Amount: `1000`, Deductible: `100` | `200 OK` | • `claim_id` matches request path.<br/>• `settlement == 900` (integer value).<br/>• `currency == "TEST"`. | **Automated** (`ClaimsApiTest`) |
| **TC-API-010** | `POST /claims/{id}/calculate` | **P0** | Boundary calculation: Amount equals Deductible | Amount: `100`, Deductible: `100` | `200 OK` | • `settlement == 0`. | **Automated** (`ClaimsApiTest`) |
| **TC-API-011** | `POST /claims/{id}/calculate` | **P0** | Boundary calculation: Amount less than Deductible | Amount: `50`, Deductible: `100` | `200 OK` | • `settlement == 0` (enforces non-negative floor rule). | **Automated** (`ClaimsApiTest`) |
| **TC-API-012** | `POST /claims/{id}/calculate` | **P0** | Verify calculation idempotency (no mutation on re-run) | Call `/calculate` twice consecutively on same claim | `200 OK` | • Both invocations return identical settlement value.<br/>• No state mutation or duplicate deduction occurs. | **Automated** (`ClaimsApiTest`) |
| **TC-API-013** | `POST /claims/{id}/calculate` | **P1** | Attempt settlement calculation on non-existent claim | Path param: `id = does-not-exist` | `404 Not Found` | • `{"detail": "Claim not found"}` | **Automated** (`ClaimsApiTest`) |
| **TC-API-014** | `POST /integrations/assessor` | **P0** | Ingest third-party assessor approval payload | `{"claim_id":"CLM-1", "assessor_id":"ASSESSOR-1", "assessment":{"approved":true}}` | `202 Accepted` | • `accepted == true`.<br/>• `correlation_id` contains prefix `CORR-` with unique suffix. | **Automated** (`ClaimsApiTest`) |
| **TC-API-015** | `POST /integrations/assessor` | **P0** | Reject third-party assessor payload with missing `claim_id` | `{"assessor_id":"ASSESSOR-1", "assessment":{"approved":true}}` | `422 / 400` | • Ingestion blocked due to missing mandatory claim reference. | **Automated** (`ClaimsApiTest`) |
| **TC-API-016** | `POST /integrations/assessor` | **P1** | Reject third-party assessor payload with empty `assessment` | `{"claim_id":"CLM-1", "assessor_id":"ASSESSOR-1"}` | `422 / 400` | • Ingestion blocked due to missing assessment metadata. | **Manual** |
