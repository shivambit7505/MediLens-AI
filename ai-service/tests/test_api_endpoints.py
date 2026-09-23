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
