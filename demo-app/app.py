from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field
from typing import Optional
import uuid

app = FastAPI(title="Insurance Claims Demo Service")
claims = {}

class ClaimCreate(BaseModel):
    policy_id: str = Field(min_length=1, max_length=50)
    product: str
    claim_amount: float = Field(ge=0)
    deductible: float = Field(ge=0)
    incident_description: Optional[str] = Field(default="Standard claim submission", max_length=200)
    claimant_email: Optional[str] = Field(default="claimant@example.com", max_length=100)

@app.get("/", response_class=HTMLResponse)
def home():
    return '''<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Insurance Claims Demo</title>
  <style>
    :root {
      --primary: #2563eb;
      --primary-hover: #1d4ed8;
      --secondary: #0d9488;
      --secondary-hover: #0f766e;
      --bg: #f8fafc;
      --card-bg: #ffffff;
      --text: #1e293b;
      --text-muted: #64748b;
      --border: #cbd5e1;
      --border-focus: #3b82f6;
      --badge-bg: #e0f2fe;
      --badge-text: #0369a1;
      --error-bg: #fee2e2;
      --error-text: #dc2626;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    }

    body {
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      justify-content: center;
      align-items: flex-start;
      padding: 2.5rem 1rem;
    }

    .container {
      background: var(--card-bg);
      border-radius: 12px;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04);
      width: 100%;
      max-width: 520px;
      padding: 2rem;
      border: 1px solid #e2e8f0;
    }

    h1 {
      font-size: 1.5rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 1.5rem;
      padding-bottom: 0.75rem;
      border-bottom: 2px solid #f1f5f9;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      margin-bottom: 1.25rem;
      gap: 0.4rem;
    }

    label {
      font-size: 0.875rem;
      font-weight: 600;
      color: #334155;
    }

    input, select {
      width: 100%;
      padding: 0.65rem 0.85rem;
      border: 1.5px solid var(--border);
      border-radius: 8px;
      font-size: 0.95rem;
      color: var(--text);
      background-color: #fff;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    input:focus, select:focus {
      outline: none;
      border-color: var(--border-focus);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18);
    }

    .btn {
      display: inline-block;
      width: 100%;
      padding: 0.75rem 1.25rem;
      border: none;
      border-radius: 8px;
      font-size: 0.95rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      text-align: center;
    }

    .btn-primary {
      background-color: var(--primary);
      color: #ffffff;
      margin-top: 0.5rem;
      margin-bottom: 1.5rem;
    }

    .btn-primary:hover {
      background-color: var(--primary-hover);
    }

    .btn-secondary {
      background-color: var(--secondary);
      color: #ffffff;
      margin-top: 1rem;
    }

    .btn-secondary:hover {
      background-color: var(--secondary-hover);
    }

    .results-card {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 1.25rem;
      margin-top: 0.5rem;
    }

    .result-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.45rem 0;
      font-size: 0.9rem;
      border-bottom: 1px dashed #e2e8f0;
    }

    .result-row:last-of-type {
      border-bottom: none;
    }

    .result-label {
      font-weight: 600;
      color: var(--text-muted);
    }

    .result-value {
      font-weight: 600;
      color: var(--text);
    }

    .badge {
      background-color: var(--badge-bg);
      color: var(--badge-text);
      padding: 0.2rem 0.6rem;
      border-radius: 9999px;
      font-size: 0.8rem;
    }

    #message:not(:empty) {
      display: block;
      margin-top: 1rem;
      padding: 0.65rem 0.85rem;
      border-radius: 6px;
      background: var(--error-bg);
      color: var(--error-text);
      font-size: 0.875rem;
      font-weight: 500;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>🛡️ Insurance Claims Demo</h1>
    
    <form id="claim-form">
      <div class="form-group">
        <label for="policy-id">Policy ID</label>
        <input id="policy-id" name="policy_id" aria-label="Policy ID" placeholder="e.g. POL-1001" required />
      </div>

      <div class="form-group">
        <label for="product">Product</label>
        <select id="product" name="product" aria-label="Product">
          <option value="auto">auto</option>
          <option value="health">health</option>
          <option value="property">property</option>
        </select>
      </div>

      <div class="form-group">
        <label for="claim-amount">Claim amount</label>
        <input id="claim-amount" type="number" name="claim_amount" aria-label="Claim amount" placeholder="e.g. 1000" min="0" step="any" required />
      </div>

      <div class="form-group">
        <label for="deductible">Deductible</label>
        <input id="deductible" type="number" name="deductible" aria-label="Deductible" placeholder="e.g. 100" min="0" step="any" required />
      </div>

      <div class="form-group">
        <label for="incident-description">Incident Description <span style="font-size:0.75rem; color:#10b981; font-weight:normal;">(New QA Feature)</span></label>
        <input id="incident-description" name="incident_description" aria-label="Incident Description" placeholder="e.g. Minor car collision at intersection" />
      </div>

      <div class="form-group">
        <label for="claimant-email">Claimant Email <span style="font-size:0.75rem; color:#2563eb; font-weight:normal;">(New QA Field #2)</span></label>
        <input id="claimant-email" type="email" name="claimant_email" aria-label="Claimant Email" placeholder="e.g. claimant@claims.com" />
      </div>

      <button id="submit-claim" class="btn btn-primary" type="submit">Submit claim</button>
    </form>

    <div class="results-card">
      <div class="result-row">
        <span class="result-label">Claim ID:</span>
        <span id="claim-id" class="result-value" data-testid="claim-id"></span>
      </div>
      <div class="result-row">
        <span class="result-label">Status:</span>
        <span id="claim-status" class="result-value badge" data-testid="claim-status"></span>
      </div>
      <div class="result-row">
        <span class="result-label">Email:</span>
        <span id="claim-email" class="result-value" data-testid="claim-email">-</span>
      </div>
      <div class="result-row">
        <span class="result-label">Description:</span>
        <span id="claim-desc" class="result-value" data-testid="claim-desc">-</span>
      </div>
      <div class="result-row">
        <span class="result-label">Settlement:</span>
        <span id="settlement" class="result-value" data-testid="settlement"></span>
      </div>

      <button id="calculate-claim" class="btn btn-secondary" type="button">Calculate claim</button>
      
      <p id="message-container" style="margin-top: 0.5rem;"><span id="message"></span></p>
    </div>
  </div>

  <script>
    let claimId = null;
    const byId = id => document.getElementById(id);

    byId('claim-form').addEventListener('submit', async e => {
      e.preventDefault();
      byId('message').textContent = '';
      const body = {
        policy_id: byId('policy-id').value,
        product: byId('product').value,
        claim_amount: Number(byId('claim-amount').value),
        deductible: Number(byId('deductible').value),
        incident_description: byId('incident-description').value || undefined,
        claimant_email: byId('claimant-email').value || undefined
      };
      
      try {
        const r = await fetch('/claims', {
          method: 'POST',
          headers: {'content-type': 'application/json'},
          body: JSON.stringify(body)
        });
        const data = await r.json();
        if (!r.ok) {
          byId('message').textContent = data.detail || 'Validation error';
          return;
        }
        claimId = data.id;
        byId('claim-id').textContent = data.id;
        byId('claim-status').textContent = data.status;
        byId('claim-desc').textContent = data.incident_description || 'Standard claim submission';
        byId('claim-email').textContent = data.claimant_email || 'claimant@example.com';
      } catch (err) {
        byId('message').textContent = 'Network error occurred';
      }
    });

    byId('calculate-claim').addEventListener('click', async () => {
      if (!claimId) {
        byId('message').textContent = 'Submit a claim first';
        return;
      }
      try {
        const r = await fetch('/claims/' + claimId + '/calculate', {method: 'POST'});
        const data = await r.json();
        if (!r.ok) {
          byId('message').textContent = data.detail || 'Calculation error';
          return;
        }
        byId('settlement').textContent = String(data.settlement);
      } catch (err) {
        byId('message').textContent = 'Calculation error occurred';
      }
    });
  </script>
</body>
</html>'''

@app.post("/claims", status_code=201)
def create_claim(claim: ClaimCreate):
    cid = "CLM-" + uuid.uuid4().hex[:10].upper()
    claims[cid] = {**claim.model_dump(), "id": cid, "status": "SUBMITTED"}
    return claims[cid]

@app.get("/claims/{claim_id}")
def get_claim(claim_id: str):
    if claim_id not in claims: raise HTTPException(status_code=404, detail="Claim not found")
    return claims[claim_id]

@app.get("/claims")
def list_claims(policy_id: Optional[str] = None):
    items=list(claims.values())
    if policy_id: items=[c for c in items if c["policy_id"]==policy_id]
    return {"items":items,"count":len(items)}

class AssessorRequest(BaseModel):
    claim_id: str = Field(min_length=1)
    assessor_id: str = Field(min_length=1)
    assessment: dict

@app.post("/claims/{claim_id}/calculate")
def calculate_claim(claim_id: str):
    if claim_id not in claims: raise HTTPException(status_code=404, detail="Claim not found")
    claim=claims[claim_id]
    diff = claim["claim_amount"] - claim["deductible"]
    settlement = max(0, diff)
    if isinstance(settlement, (int, float)) and float(settlement).is_integer():
        settlement = int(settlement)
    return {"claim_id":claim_id,"settlement":settlement,"currency":"TEST"}

@app.post("/integrations/assessor", status_code=202)
def assessor_integration(payload: AssessorRequest):
    return {
        "accepted": True,
        "correlation_id": "CORR-" + uuid.uuid4().hex[:8].upper()
    }

