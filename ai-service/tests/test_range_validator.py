import pytest
from app.validation.range_validator import DeterministicRangeValidator


@pytest.fixture
def validator():
    return DeterministicRangeValidator()


def test_normal_value(validator):
    # Adult male Hemoglobin: 15.0 g/dL is within 13.8 - 17.2
    status, rule = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=15.0,
        normalized_unit="g/dL",
        patient_age_years=35.0,
        patient_gender="MALE",
    )
    assert status == "NORMAL"
    assert rule is not None
    assert rule.low_value == 13.8
    assert rule.high_value == 17.2


def test_low_value(validator):
    # Adult male Hemoglobin: 12.5 g/dL is below 13.8, but above critical low 7.0
    status, _ = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=12.5,
        normalized_unit="g/dL",
        patient_age_years=35.0,
        patient_gender="MALE",
    )
    assert status == "LOW"


def test_high_value(validator):
    # Adult Fasting Glucose: 115.0 mg/dL is above high (99.0), but below critical high (400.0)
    status, _ = validator.validate(
        canonical_name="Fasting Blood Glucose",
        normalized_value=115.0,
        normalized_unit="mg/dL",
        patient_age_years=40.0,
        patient_gender="FEMALE",
    )
    assert status == "HIGH"


def test_critical_low_boundary(validator):
    # Serum Potassium critical low is <= 2.8 mmol/L
    status, _ = validator.validate(
        canonical_name="Serum Potassium",
        normalized_value=2.5,
        normalized_unit="mmol/L",
        patient_age_years=50.0,
        patient_gender="ALL",
    )
    assert status == "CRITICAL"


def test_critical_high_boundary(validator):
    # Fasting Glucose >= 400.0 mg/dL is critical high
    status, _ = validator.validate(
        canonical_name="Fasting Blood Glucose",
        normalized_value=450.0,
        normalized_unit="mg/dL",
        patient_age_years=50.0,
        patient_gender="MALE",
    )
    assert status == "CRITICAL"


def test_demographic_gender_differentiation(validator):
    # 13.0 g/dL Hemoglobin:
    # For Adult Male (13.8 - 17.2): 13.0 is LOW
    status_male, rule_male = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=13.0,
        normalized_unit="g/dL",
        patient_age_years=30.0,
        patient_gender="MALE",
    )
    assert status_male == "LOW"
    assert rule_male.gender_applicable == "MALE"

    # For Adult Female (12.1 - 15.1): 13.0 is NORMAL
    status_female, rule_female = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=13.0,
        normalized_unit="g/dL",
        patient_age_years=30.0,
        patient_gender="FEMALE",
    )
    assert status_female == "NORMAL"
    assert rule_female.gender_applicable == "FEMALE"


def test_failed_conversion_yields_unknown(validator):
    status, rule = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=14.0,
        normalized_unit="g/dL",
        patient_age_years=30.0,
        patient_gender="MALE",
        conversion_successful=False,
    )
    assert status == "UNKNOWN"
    assert rule is None


def test_unit_mismatch_yields_unknown(validator):
    status, rule = validator.validate(
        canonical_name="Hemoglobin",
        normalized_value=14.0,
        normalized_unit="unexpected_unit",
        patient_age_years=30.0,
        patient_gender="MALE",
    )
    assert status == "UNKNOWN"
    assert rule is None
