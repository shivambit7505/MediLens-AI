from typing import Optional, Tuple
import re


class UnitConverter:
    """
    Deterministic clinical unit conversion engine.
    Standardizes varied international lab units into canonical reference units.
    Never mutates original extracted strings; produces deterministic normalized values.
    """

    @staticmethod
    def clean_unit(unit: str) -> str:
        if not unit:
            return ""
        u = unit.strip().lower()
        u = re.sub(r"\s+", "", u)
        u = u.replace("micro", "u").replace("µ", "u")
        return u

    def convert(
        self,
        canonical_name: str,
        value: float,
        from_unit: Optional[str],
        standard_unit: str,
    ) -> Tuple[float, str, bool]:
        """
        Converts an observed value from its extracted unit to the biomarker's standard unit.
        Returns:
            (normalized_value, normalized_unit, is_converted_successfully)
        """
        if not from_unit:
            # Missing unit: cannot deterministically convert
            return value, standard_unit, False

        clean_from = self.clean_unit(from_unit)
        clean_std = self.clean_unit(standard_unit)

        # 1. Identity match (units are already identical)
        if clean_from == clean_std:
            return round(value, 4), standard_unit, True

        # 2. Glucose (standard: mg/dL)
        if canonical_name == "Fasting Blood Glucose":
            if clean_from in ("mmol/l", "mm/l"):
                # 1 mmol/L = 18.0182 mg/dL
                return round(value * 18.0182, 2), "mg/dL", True
            if clean_from in ("g/l",):
                return round(value * 100.0, 2), "mg/dL", True

        # 3. Total Cholesterol, HDL, LDL, Triglycerides (standard: mg/dL)
        if canonical_name in ("Total Cholesterol", "HDL Cholesterol", "LDL Cholesterol", "Triglycerides"):
            if clean_from in ("mmol/l", "mm/l"):
                # 1 mmol/L = 38.67 mg/dL for cholesterol
                factor = 38.67 if canonical_name != "Triglycerides" else 88.57
                return round(value * factor, 2), "mg/dL", True
            if clean_from in ("g/l",):
                return round(value * 100.0, 2), "mg/dL", True

        # 4. Serum Creatinine (standard: mg/dL)
        if canonical_name == "Serum Creatinine":
            if clean_from in ("umol/l", "micromol/l", "mcmol/l"):
                # 1 umol/L = 0.011312 mg/dL
                return round(value * 0.011312, 3), "mg/dL", True

        # 5. Hemoglobin (standard: g/dL)
        if canonical_name == "Hemoglobin":
            if clean_from in ("g/l",):
                return round(value * 0.1, 2), "g/dL", True
            if clean_from in ("mmol/l",):
                return round(value * 1.6114, 2), "g/dL", True

        # 6. Electrolytes: Potassium, Sodium (standard: mmol/L)
        if canonical_name in ("Serum Potassium", "Serum Sodium"):
            if clean_from in ("meq/l", "meq"):
                # 1 mEq/L = 1 mmol/L for monovalent cations (Na+, K+)
                return round(value, 2), "mmol/L", True

        # 7. Complete Blood Count: Platelets, WBC (standard: 10^3/uL)
        if canonical_name in ("Platelet Count", "White Blood Cell Count"):
            if clean_from in ("10^9/l", "x10^9/l", "g/l", "k/ul", "thous/mcl", "k/cumm"):
                return round(value, 2), "10^3/uL", True
            if clean_from in ("/ul", "cells/ul", "/cumm"):
                return round(value / 1000.0, 2), "10^3/uL", True

        # 8. Liver Enzymes: ALT, AST (standard: U/L)
        if canonical_name in ("Alanine Aminotransferase", "Aspartate Aminotransferase"):
            if clean_from in ("iu/l", "u/l"):
                return round(value, 2), "U/L", True

        # Unrecognized or non-standard conversion: return original, marked as unconverted
        return value, from_unit, False
