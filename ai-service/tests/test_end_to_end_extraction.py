import pytest
from app.extraction.extractor import BiomarkerExtractor


@pytest.fixture
def extractor():
    return BiomarkerExtractor()


def test_extract_cbc_report(extractor):
    raw_text = """
    CENTRAL DIAGNOSTIC LABORATORY
    Patient: John Doe, Age: 42, Sex: Male
    --------------------------------------------------------
    Test Name                   Result      Units       Reference Interval
    Hemoglobin                  14.5        g/dL        13.8 - 17.2
    White Blood Cell Count      6.8         10^3/uL     4.0 - 11.0
    Platelet Count              240.0       10^3/uL     150 - 450
    --------------------------------------------------------
    End of Report
    """
    response = extractor.extract_from_text(
        raw_text=raw_text,
        patient_age_years=42.0,
        patient_gender="MALE",
    )

    assert response.extracted_count >= 3
    names = {m.canonical_name: m for m in response.measurements}

    assert "Hemoglobin" in names
    hb = names["Hemoglobin"]
    assert hb.observed_value_numeric == 14.5
    assert hb.normalized_unit == "g/dL"
    assert hb.status == "NORMAL"
    assert hb.code_loinc == "718-7"

    assert "White Blood Cell Count" in names
    wbc = names["White Blood Cell Count"]
    assert wbc.observed_value_numeric == 6.8
    assert wbc.status == "NORMAL"

    assert "Platelet Count" in names
    plt = names["Platelet Count"]
    assert plt.observed_value_numeric == 240.0
    assert plt.status == "NORMAL"


def test_extract_international_units_metabolic(extractor):
    raw_text = """
    METABOLIC PANEL
    Patient: Jane Doe, Age: 48, Sex: Female
    Fasting Blood Glucose       5.5     mmol/L      70-99
    Serum Creatinine            0.85    mg/dL       0.59-1.04
    Serum Potassium             4.1     mmol/L      3.5-5.0
    """
    response = extractor.extract_from_text(
        raw_text=raw_text,
        patient_age_years=48.0,
        patient_gender="FEMALE",
    )

    names = {m.canonical_name: m for m in response.measurements}

    # Glucose in mmol/L should be converted to mg/dL (5.5 * 18.0182 = 99.1 mg/dL)
    assert "Fasting Blood Glucose" in names
    fbg = names["Fasting Blood Glucose"]
    assert fbg.observed_value_numeric == 5.5
    assert fbg.extracted_unit.lower() == "mmol/l"
    assert fbg.normalized_unit == "mg/dL"
    assert fbg.normalized_value_numeric == 99.1

    assert "Serum Creatinine" in names
    cr = names["Serum Creatinine"]
    assert cr.observed_value_numeric == 0.85
    assert cr.status == "NORMAL"

    assert "Serum Potassium" in names
    k = names["Serum Potassium"]
    assert k.observed_value_numeric == 4.1
    assert k.status == "NORMAL"


def test_critical_value_detection(extractor):
    raw_text = """
    CRITICAL CARE LAB ALERT
    Patient: Adult, Age: 60, Sex: ALL
    Serum Potassium             2.4     mmol/L      3.5-5.0
    Fasting Blood Glucose       480.0   mg/dL       70-99
    """
    response = extractor.extract_from_text(
        raw_text=raw_text,
        patient_age_years=60.0,
        patient_gender="ALL",
    )

    names = {m.canonical_name: m for m in response.measurements}
    assert "Serum Potassium" in names
    assert names["Serum Potassium"].status == "CRITICAL"

    assert "Fasting Blood Glucose" in names
    assert names["Fasting Blood Glucose"].status == "CRITICAL"
