from typing import List, Optional
from pydantic import BaseModel


class ClinicalRangeRule(BaseModel):
    canonical_name: str
    gender_applicable: str = "ALL"  # 'ALL', 'MALE', 'FEMALE'
    age_min_years: float = 0.0
    age_max_years: float = 150.0
    unit: str
    low_value: float
    high_value: float
    critical_low: Optional[float] = None
    critical_high: Optional[float] = None
    source_citation: str


# Authoritative reference intervals stratified by demographics
CANONICAL_REFERENCE_RANGES: List[ClinicalRangeRule] = [
    # Hemoglobin (Adult Male)
    ClinicalRangeRule(
        canonical_name="Hemoglobin",
        gender_applicable="MALE",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="g/dL",
        low_value=13.8,
        high_value=17.2,
        critical_low=7.0,
        critical_high=20.0,
        source_citation="Mayo Clinic Laboratories Reference Manual 2024",
    ),
    # Hemoglobin (Adult Female)
    ClinicalRangeRule(
        canonical_name="Hemoglobin",
        gender_applicable="FEMALE",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="g/dL",
        low_value=12.1,
        high_value=15.1,
        critical_low=7.0,
        critical_high=20.0,
        source_citation="Mayo Clinic Laboratories Reference Manual 2024",
    ),
    # Hemoglobin (Generic/Default)
    ClinicalRangeRule(
        canonical_name="Hemoglobin",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="g/dL",
        low_value=12.0,
        high_value=17.0,
        critical_low=7.0,
        critical_high=20.0,
        source_citation="World Health Organization (WHO) Diagnostic Standards",
    ),
    # Fasting Blood Glucose (All Adults)
    ClinicalRangeRule(
        canonical_name="Fasting Blood Glucose",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mg/dL",
        low_value=70.0,
        high_value=99.0,
        critical_low=50.0,
        critical_high=400.0,
        source_citation="American Diabetes Association (ADA) Standards of Care 2024",
    ),
    # Hemoglobin A1c (All Adults)
    ClinicalRangeRule(
        canonical_name="Hemoglobin A1c",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="%",
        low_value=4.0,
        high_value=5.6,
        critical_low=None,
        critical_high=14.0,
        source_citation="American Diabetes Association (ADA) 2024",
    ),
    # Serum Creatinine (Adult Male)
    ClinicalRangeRule(
        canonical_name="Serum Creatinine",
        gender_applicable="MALE",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mg/dL",
        low_value=0.74,
        high_value=1.35,
        critical_low=None,
        critical_high=4.00,
        source_citation="National Kidney Foundation (KDOQI) Guidelines",
    ),
    # Serum Creatinine (Adult Female)
    ClinicalRangeRule(
        canonical_name="Serum Creatinine",
        gender_applicable="FEMALE",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mg/dL",
        low_value=0.59,
        high_value=1.04,
        critical_low=None,
        critical_high=4.00,
        source_citation="National Kidney Foundation (KDOQI) Guidelines",
    ),
    # Serum Potassium (All Adults)
    ClinicalRangeRule(
        canonical_name="Serum Potassium",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mmol/L",
        low_value=3.5,
        high_value=5.0,
        critical_low=2.8,
        critical_high=6.5,
        source_citation="WHO Essential Diagnostics Guidelines",
    ),
    # Serum Sodium (All Adults)
    ClinicalRangeRule(
        canonical_name="Serum Sodium",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mmol/L",
        low_value=135.0,
        high_value=145.0,
        critical_low=120.0,
        critical_high=160.0,
        source_citation="Clinical Chemistry Reference Handbook",
    ),
    # Total Cholesterol (All Adults)
    ClinicalRangeRule(
        canonical_name="Total Cholesterol",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="mg/dL",
        low_value=100.0,
        high_value=199.0,
        critical_low=None,
        critical_high=400.0,
        source_citation="National Cholesterol Education Program (NCEP) ATP III",
    ),
    # Platelet Count (All Adults)
    ClinicalRangeRule(
        canonical_name="Platelet Count",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="10^3/uL",
        low_value=150.0,
        high_value=450.0,
        critical_low=20.0,
        critical_high=1000.0,
        source_citation="International Council for Standardization in Haematology",
    ),
    # White Blood Cell Count (All Adults)
    ClinicalRangeRule(
        canonical_name="White Blood Cell Count",
        gender_applicable="ALL",
        age_min_years=18.0,
        age_max_years=120.0,
        unit="10^3/uL",
        low_value=4.0,
        high_value=11.0,
        critical_low=1.5,
        critical_high=30.0,
        source_citation="International Council for Standardization in Haematology",
    ),
]
