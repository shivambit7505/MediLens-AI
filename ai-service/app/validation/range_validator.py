from typing import Optional, Tuple
from app.validation.rules_catalog import CANONICAL_REFERENCE_RANGES, ClinicalRangeRule


class DeterministicRangeValidator:
    """
    Deterministic clinical reference-range validation engine.
    Compares normalized values against demographic-stratified clinical guidelines.
    Evaluates readings strictly into: LOW, NORMAL, HIGH, CRITICAL, or UNKNOWN.
    """

    def find_reference_range(
        self,
        canonical_name: str,
        patient_age_years: float,
        patient_gender: str,
    ) -> Optional[ClinicalRangeRule]:
        """
        Finds the most specific demographic reference interval for the given biomarker, age, and sex.
        """
        gender_norm = patient_gender.strip().upper()
        candidates = [
            r for r in CANONICAL_REFERENCE_RANGES
            if r.canonical_name == canonical_name
            and r.age_min_years <= patient_age_years <= r.age_max_years
        ]

        # Prioritize exact sex match over 'ALL'
        for r in candidates:
            if r.gender_applicable == gender_norm:
                return r

        # Fallback to 'ALL'
        for r in candidates:
            if r.gender_applicable == "ALL":
                return r

        return None

    def validate(
        self,
        canonical_name: str,
        normalized_value: float,
        normalized_unit: str,
        patient_age_years: float = 30.0,
        patient_gender: str = "ALL",
        conversion_successful: bool = True,
    ) -> Tuple[str, Optional[ClinicalRangeRule]]:
        """
        Deterministically evaluates status:
        Returns:
            (status: 'LOW'|'NORMAL'|'HIGH'|'CRITICAL'|'UNKNOWN', matched_rule)
        """
        # If conversion failed or unit is invalid, status must be UNKNOWN
        if not conversion_successful:
            return "UNKNOWN", None

        rule = self.find_reference_range(canonical_name, patient_age_years, patient_gender)
        if not rule:
            return "UNKNOWN", None

        # Check unit compatibility
        if rule.unit.lower() != normalized_unit.lower():
            return "UNKNOWN", None

        # 1. Critical Boundaries (Immediate clinical red-flags)
        if rule.critical_low is not None and normalized_value <= rule.critical_low:
            return "CRITICAL", rule

        if rule.critical_high is not None and normalized_value >= rule.critical_high:
            return "CRITICAL", rule

        # 2. Out-of-range boundaries
        if normalized_value < rule.low_value:
            return "LOW", rule

        if normalized_value > rule.high_value:
            return "HIGH", rule

        # 3. In-range normal
        if rule.low_value <= normalized_value <= rule.high_value:
            return "NORMAL", rule

        return "UNKNOWN", rule
