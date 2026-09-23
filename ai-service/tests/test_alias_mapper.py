import pytest
from app.normalization.alias_dictionary import AliasResolver


@pytest.fixture
def resolver():
    return AliasResolver()


def test_exact_canonical_name(resolver):
    matched = resolver.resolve("Hemoglobin")
    assert matched is not None
    assert matched.canonical_name == "Hemoglobin"
    assert matched.code_loinc == "718-7"


def test_abbreviations_and_acronyms(resolver):
    test_cases = [
        ("hb", "Hemoglobin"),
        ("hgb", "Hemoglobin"),
        ("fbs", "Fasting Blood Glucose"),
        ("fbg", "Fasting Blood Glucose"),
        ("hba1c", "Hemoglobin A1c"),
        ("cr", "Serum Creatinine"),
        ("plt", "Platelet Count"),
        ("wbc", "White Blood Cell Count"),
        ("alt", "Alanine Aminotransferase"),
        ("sgpt", "Alanine Aminotransferase"),
        ("ast", "Aspartate Aminotransferase"),
        ("sgot", "Aspartate Aminotransferase"),
        ("tg", "Triglycerides"),
        ("k", "Serum Potassium"),
        ("na", "Serum Sodium"),
    ]
    for raw, expected in test_cases:
        res = resolver.resolve(raw)
        assert res is not None, f"Failed to resolve alias: {raw}"
        assert res.canonical_name == expected


def test_punctuation_and_case_insensitivity(resolver):
    test_cases = [
        ("S. Creatinine", "Serum Creatinine"),
        ("Hb.", "Hemoglobin"),
        ("FASTING BLOOD SUGAR", "Fasting Blood Glucose"),
        ("HDL-C", "HDL Cholesterol"),
        ("LDL-C", "LDL Cholesterol"),
        ("Total Leukocyte Count (TLC)", "White Blood Cell Count"),
    ]
    for raw, expected in test_cases:
        res = resolver.resolve(raw)
        assert res is not None, f"Failed to resolve noisy string: {raw}"
        assert res.canonical_name == expected


def test_unrecognized_returns_none(resolver):
    assert resolver.resolve("Unknown Research Marker 123") is None
    assert resolver.resolve("") is None
    assert resolver.resolve("   ") is None
