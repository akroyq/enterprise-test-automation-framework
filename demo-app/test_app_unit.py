"""
FastAPI Claims Microservice - Core Unit Test Suite
Covers: Pydantic Validation, Settlement Rules, Calculation Engine, REST API Contracts, Assessor Integration
Execution SLA: < 2 seconds (Fast Pre-Merge Developer Feedback)
"""
import pytest
from fastapi.testclient import TestClient
from app import app, claims

client = TestClient(app)

@pytest.fixture(autouse=True)
def clean_claims_store():
    """Clear in-memory claims dictionary before each unit test to ensure isolation."""
    claims.clear()
    yield
    claims.clear()

def test_home_page_returns_html():
    """Verify home endpoint serves the HTML UI with HTTP 200."""
    response = client.get("/")
    assert response.status_code == 200
    assert "Insurance Claims Demo" in response.text
    assert "<form id=\"claim-form\"" in response.text

def test_create_claim_success():
    """Verify valid claim submission returns HTTP 201 with SUBMITTED status."""
    payload = {
        "policy_id": "POL-UNIT-001",
        "product": "Auto Collision",
        "claim_amount": 2500.0,
        "deductible": 500.0
    }
    response = client.post("/claims", json=payload)
    assert response.status_code == 201
    data = response.json()
    assert "id" in data
    assert data["id"].startswith("CLM-")
    assert data["policy_id"] == "POL-UNIT-001"
    assert data["product"] == "Auto Collision"
    assert data["claim_amount"] == 2500.0
    assert data["deductible"] == 500.0
    assert data["status"] == "SUBMITTED"

def test_create_claim_validation_negative_claim_amount():
    """Verify validation failure (422) when claim_amount is negative."""
    payload = {
        "policy_id": "POL-UNIT-003",
        "product": "Auto Collision",
        "claim_amount": -100.0,
        "deductible": 50.0
    }
    response = client.post("/claims", json=payload)
    assert response.status_code == 422

def test_create_claim_validation_negative_deductible():
    """Verify validation failure (422) when deductible is negative."""
    payload = {
        "policy_id": "POL-UNIT-004",
        "product": "Health",
        "claim_amount": 1000.0,
        "deductible": -250.0
    }
    response = client.post("/claims", json=payload)
    assert response.status_code == 422

def test_create_claim_validation_empty_policy_id():
    """Verify validation failure (422) when policy_id is empty string."""
    payload = {
        "policy_id": "",
        "product": "Auto",
        "claim_amount": 500.0,
        "deductible": 100.0
    }
    response = client.post("/claims", json=payload)
    assert response.status_code == 422

def test_calculate_claim_approved():
    """Verify calculation rule: settlement = claim_amount - deductible (> 0)."""
    create_res = client.post("/claims", json={
        "policy_id": "POL-CALC-001",
        "product": "Auto Comprehensive",
        "claim_amount": 4000.0,
        "deductible": 1000.0
    })
    claim_id = create_res.json()["id"]

    calc_res = client.post(f"/claims/{claim_id}/calculate")
    assert calc_res.status_code == 200
    data = calc_res.json()
    assert data["claim_id"] == claim_id
    assert data["settlement"] == 3000
    assert data["currency"] == "TEST"

def test_calculate_claim_rejected_when_deductible_exceeds_amount():
    """Verify calculation rule: claim_amount <= deductible -> settlement 0."""
    create_res = client.post("/claims", json={
        "policy_id": "POL-CALC-002",
        "product": "Travel",
        "claim_amount": 300.0,
        "deductible": 500.0
    })
    claim_id = create_res.json()["id"]

    calc_res = client.post(f"/claims/{claim_id}/calculate")
    assert calc_res.status_code == 200
    data = calc_res.json()
    assert data["claim_id"] == claim_id
    assert data["settlement"] == 0
    assert data["currency"] == "TEST"

def test_calculate_claim_boundary_equal_amount_and_deductible():
    """Verify boundary condition: claim_amount == deductible -> settlement 0."""
    create_res = client.post("/claims", json={
        "policy_id": "POL-CALC-003",
        "product": "Dental",
        "claim_amount": 250.0,
        "deductible": 250.0
    })
    claim_id = create_res.json()["id"]

    calc_res = client.post(f"/claims/{claim_id}/calculate")
    assert calc_res.status_code == 200
    data = calc_res.json()
    assert data["claim_id"] == claim_id
    assert data["settlement"] == 0

def test_calculate_nonexistent_claim_returns_404():
    """Verify calculate endpoint returns 404 for unknown claim IDs."""
    response = client.post("/claims/NON-EXISTENT-ID/calculate")
    assert response.status_code == 404
    assert response.json()["detail"] == "Claim not found"

def test_get_claim_by_id_success():
    """Verify retrieving an existing claim returns correct payload."""
    create_res = client.post("/claims", json={
        "policy_id": "POL-GET-001",
        "product": "Property",
        "claim_amount": 15000.0,
        "deductible": 1000.0
    })
    claim_id = create_res.json()["id"]

    get_res = client.get(f"/claims/{claim_id}")
    assert get_res.status_code == 200
    assert get_res.json()["id"] == claim_id
    assert get_res.json()["policy_id"] == "POL-GET-001"

def test_get_claim_by_id_not_found():
    """Verify retrieving unknown claim returns HTTP 404."""
    response = client.get("/claims/INVALID-999")
    assert response.status_code == 404

def test_list_all_claims():
    """Verify GET /claims lists all created claims."""
    client.post("/claims", json={"policy_id": "POL-1", "product": "Auto", "claim_amount": 100.0, "deductible": 10.0})
    client.post("/claims", json={"policy_id": "POL-2", "product": "Home", "claim_amount": 200.0, "deductible": 20.0})

    response = client.get("/claims")
    assert response.status_code == 200
    result = response.json()
    assert result["count"] == 2
    assert len(result["items"]) == 2

def test_assessor_integration():
    """Verify external assessor integration endpoint returns HTTP 202 with correlation ID."""
    payload = {
        "claim_id": "CLM-ASSESS-001",
        "assessor_id": "ASSESSOR-42",
        "assessment": {"damage_score": 8, "notes": "Approved for repair"}
    }
    response = client.post("/integrations/assessor", json=payload)
    assert response.status_code == 202
    data = response.json()
    assert data["accepted"] is True
    assert data["correlation_id"].startswith("CORR-")
