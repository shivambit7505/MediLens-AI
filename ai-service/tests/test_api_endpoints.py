import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.core.config import settings


@pytest.fixture
def client():
    return TestClient(app)


def test_health_check_endpoint(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["service"] == "medilens-ai-service"


def test_biomarker_extraction_without_auth_header(client):
    payload = {
        "raw_ocr_text": "Hemoglobin 14.5 g/dL",
        "patient_age_years": 30.0,
        "patient_gender": "MALE",
    }
    response = client.post("/internal/v1/biomarkers/extract-and-validate", json=payload)
    assert response.status_code == 401
    assert "Invalid or missing X-Internal-API-Key" in response.json()["detail"]


def test_biomarker_extraction_with_invalid_auth_key(client):
    payload = {
        "raw_ocr_text": "Hemoglobin 14.5 g/dL",
        "patient_age_years": 30.0,
        "patient_gender": "MALE",
    }
    headers = {"X-Internal-API-Key": "wrong-secret-key"}
    response = client.post(
        "/internal/v1/biomarkers/extract-and-validate",
        json=payload,
        headers=headers,
    )
    assert response.status_code == 401


def test_biomarker_extraction_with_valid_auth(client):
    payload = {
        "raw_ocr_text": "Hemoglobin 14.5 g/dL 13.8-17.2\nPlatelet Count 250 10^3/uL 150-450",
        "patient_age_years": 35.0,
        "patient_gender": "MALE",
    }
    headers = {"X-Internal-API-Key": settings.INTERNAL_API_KEY}
    response = client.post(
        "/internal/v1/biomarkers/extract-and-validate",
        json=payload,
        headers=headers,
    )
    assert response.status_code == 200
    data = response.json()
    assert data["extracted_count"] == 2
    measurements = data["measurements"]
    assert measurements[0]["canonical_name"] == "Hemoglobin"
    assert measurements[0]["status"] == "NORMAL"
    assert measurements[1]["canonical_name"] == "Platelet Count"
    assert measurements[1]["status"] == "NORMAL"


def test_rag_explanation_endpoint(client):
    payload = {
        "report_id": "test-report-001",
        "patient_age_years": 40.0,
        "patient_gender": "FEMALE",
        "measurements": [
            {
                "canonical_name": "Hemoglobin",
                "normalized_value_numeric": 9.8,
                "normalized_unit": "g/dL",
                "reference_interval_raw": "12.1-15.1",
                "status": "LOW",
            },
            {
                "canonical_name": "Glucose",
                "normalized_value_numeric": 115.0,
                "normalized_unit": "mg/dL",
                "reference_interval_raw": "70-99",
                "status": "HIGH",
            },
        ],
    }
    headers = {"X-Internal-API-Key": settings.INTERNAL_API_KEY}
    response = client.post(
        "/internal/v1/rag/explain-report",
        json=payload,
        headers=headers,
    )
    assert response.status_code == 200
    data = response.json()
    assert data["report_id"] == "test-report-001"
    assert len(data["findings"]) == 2
    assert "disclaimer" in data
    assert len(data["cited_sources"]) > 0
    assert data["safety_audit_passed"] is True

