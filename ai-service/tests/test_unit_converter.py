import pytest
from app.normalization.unit_converter import UnitConverter


@pytest.fixture
def converter():
    return UnitConverter()


def test_identity_conversion(converter):
    # Same unit should pass through cleanly
    val, unit, success = converter.convert(
        canonical_name="Hemoglobin",
        value=14.2,
        from_unit="g/dL",
        standard_unit="g/dL",
    )
    assert success is True
    assert val == 14.2
    assert unit == "g/dL"


def test_glucose_mmol_to_mgdl(converter):
    # 5.5 mmol/L * 18.0182 = 99.10 mg/dL
    val, unit, success = converter.convert(
        canonical_name="Fasting Blood Glucose",
        value=5.5,
        from_unit="mmol/L",
        standard_unit="mg/dL",
    )
    assert success is True
    assert val == 99.1
    assert unit == "mg/dL"


def test_glucose_gl_to_mgdl(converter):
    # 0.95 g/L * 100 = 95.0 mg/dL
    val, unit, success = converter.convert(
        canonical_name="Fasting Blood Glucose",
        value=0.95,
        from_unit="g/L",
        standard_unit="mg/dL",
    )
    assert success is True
    assert val == 95.0
    assert unit == "mg/dL"


def test_hemoglobin_gl_to_gdl(converter):
    # 135 g/L * 0.1 = 13.5 g/dL
    val, unit, success = converter.convert(
        canonical_name="Hemoglobin",
        value=135.0,
        from_unit="g/L",
        standard_unit="g/dL",
    )
    assert success is True
    assert val == 13.5
    assert unit == "g/dL"


def test_creatinine_umol_to_mgdl(converter):
    # 88.4 umol/L * 0.011312 = 1.000 mg/dL
    val, unit, success = converter.convert(
        canonical_name="Serum Creatinine",
        value=88.4,
        from_unit="umol/L",
        standard_unit="mg/dL",
    )
    assert success is True
    assert round(val, 2) == 1.0
    assert unit == "mg/dL"


def test_cholesterol_mmol_to_mgdl(converter):
    # 5.0 mmol/L * 38.67 = 193.35 mg/dL
    val, unit, success = converter.convert(
        canonical_name="Total Cholesterol",
        value=5.0,
        from_unit="mmol/L",
        standard_unit="mg/dL",
    )
    assert success is True
    assert val == 193.35
    assert unit == "mg/dL"


def test_triglycerides_mmol_to_mgdl(converter):
    # 1.5 mmol/L * 88.57 = 132.86 mg/dL
    val, unit, success = converter.convert(
        canonical_name="Triglycerides",
        value=1.5,
        from_unit="mmol/L",
        standard_unit="mg/dL",
    )
    assert success is True
    assert val in (132.85, 132.86)
    assert unit == "mg/dL"


def test_potassium_meq_to_mmoll(converter):
    # 4.2 mEq/L -> 4.2 mmol/L
    val, unit, success = converter.convert(
        canonical_name="Serum Potassium",
        value=4.2,
        from_unit="mEq/L",
        standard_unit="mmol/L",
    )
    assert success is True
    assert val == 4.2
    assert unit == "mmol/L"


def test_platelets_cells_to_10_3_ul(converter):
    # 250000 /uL -> 250.0 10^3/uL
    val, unit, success = converter.convert(
        canonical_name="Platelet Count",
        value=250000.0,
        from_unit="/uL",
        standard_unit="10^3/uL",
    )
    assert success is True
    assert val == 250.0
    assert unit == "10^3/uL"


def test_missing_unit_fallback(converter):
    val, unit, success = converter.convert(
        canonical_name="Hemoglobin",
        value=14.0,
        from_unit=None,
        standard_unit="g/dL",
    )
    assert success is False
    assert val == 14.0
    assert unit == "g/dL"


def test_unknown_unit_fallback(converter):
    val, unit, success = converter.convert(
        canonical_name="Hemoglobin",
        value=14.0,
        from_unit="xyz_unknown",
        standard_unit="g/dL",
    )
    assert success is False
    assert val == 14.0
    assert unit == "xyz_unknown"
