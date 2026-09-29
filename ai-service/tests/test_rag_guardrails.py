"""
Tests for ClinicalSafetyGuardrails and MedicalRagService in MediLens AI.
Verifies refusal of diagnostic assertions, mandatory disclaimers, citation validation,
and critical emergency warnings.
"""

import pytest
from app.rag.guardrails import ClinicalSafetyGuardrail, MANDATORY_DISCLAIMER, CRITICAL_EMERGENCY_BANNER
from app.rag.models import RagExplanationRequest, RagBiomarkerInput
from app.rag.rag_service import MedicalRagService


def test_guardrail_detects_prohibited_diagnosis():
    guardrail = ClinicalSafetyGuardrail()
    
    bad_text = "Based on your high blood sugar, you have diabetes and should start treatment."
    violations = guardrail.check_prohibited_diagnoses(bad_text)
    assert len(violations) > 0
    assert any("diabetes" in v for v in violations)


def test_guardrail_sanitizes_diagnostic_claims():
    guardrail = ClinicalSafetyGuardrail()
    text = "The results indicate you have diabetes."
    sanitized = guardrail.sanitize_diagnostic_claims(text)
    assert "you have diabetes" not in sanitized.lower()
    assert "associated with" in sanitized.lower()


def test_guardrail_verifies_citations():
    guardrail = ClinicalSafetyGuardrail()
    valid_ids = {"ADA-2024-GLUCOSE", "KDIGO-2024-RENAL"}
    
    grounded_text = "Fasting glucose targets are established in [Source: ADA-2024-GLUCOSE]."
    is_valid, invalid = guardrail.verify_citations(grounded_text, valid_ids)
    assert is_valid is True
    assert len(invalid) == 0

    hallucinated_text = "Target levels are derived from [Source: FAKE-SOURCE-999]."
    is_valid_fake, invalid_fake = guardrail.verify_citations(hallucinated_text, valid_ids)
    assert is_valid_fake is False
    assert "FAKE-SOURCE-999" in invalid_fake


def test_guardrail_audit_injects_disclaimer_and_critical_banner():
    guardrail = ClinicalSafetyGuardrail()
    text = "Your potassium is 6.5 mEq/L."
    valid_ids = {"MAYO-ELECTROLYTES-POTASSIUM"}

    audit_passed, issues, audited_text = guardrail.audit_explanation(
        explanation_text=text,
        valid_source_ids=valid_ids,
        has_critical_findings=True,
    )

    assert MANDATORY_DISCLAIMER in audited_text
    assert "CRITICAL CLINICAL ALERT" in audited_text
    assert audit_passed is True


def test_rag_service_generates_grounded_explanation():
    service = MedicalRagService()

    request = RagExplanationRequest(
        report_id="rep-12345",
        patient_age_years=45.0,
        patient_gender="MALE",
        measurements=[
            RagBiomarkerInput(
                canonical_name="glucose",
                observed_value_raw="145 mg/dL",
                normalized_value_numeric=145.0,
                normalized_unit="mg/dL",
                reference_interval_raw="70-99",
                status="HIGH",
            ),
            RagBiomarkerInput(
                canonical_name="potassium",
                observed_value_raw="6.2 mmol/L",
                normalized_value_numeric=6.2,
                normalized_unit="mmol/L",
                reference_interval_raw="3.5-5.2",
                status="CRITICAL",
            ),
            RagBiomarkerInput(
                canonical_name="creatinine",
                observed_value_raw="0.9 mg/dL",
                normalized_value_numeric=0.9,
                normalized_unit="mg/dL",
                reference_interval_raw="0.7-1.3",
                status="NORMAL",
            ),
        ],
    )

    response = service.generate_explanation(request)

    assert response.report_id == "rep-12345"
    assert response.critical_alert is not None
    assert "CRITICAL CLINICAL ALERT" in response.critical_alert
    assert response.disclaimer == MANDATORY_DISCLAIMER
    assert len(response.findings) == 3
    assert len(response.cited_sources) >= 2
    assert response.safety_audit_passed is True

    # Ensure glucose finding cited ADA
    glucose_finding = next(f for f in response.findings if f.canonical_name == "glucose")
    assert "ADA-2024-GLUCOSE" in glucose_finding.sources
    assert "Source: ADA-2024-GLUCOSE" in glucose_finding.explanation

    # Ensure doctor questions are present
    assert len(response.questions_for_doctor) > 0
