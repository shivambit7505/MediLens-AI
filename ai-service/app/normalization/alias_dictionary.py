from typing import Dict, List, Optional
from pydantic import BaseModel


class CanonicalBiomarkerDef(BaseModel):
    canonical_name: str
    code_loinc: str
    code_snomed: Optional[str] = None
    category: str
    standard_unit: str
    aliases: List[str]


CANONICAL_BIOMARKERS: Dict[str, CanonicalBiomarkerDef] = {
    "Hemoglobin": CanonicalBiomarkerDef(
        canonical_name="Hemoglobin",
        code_loinc="718-7",
        code_snomed="38082009",
        category="Complete Blood Count",
        standard_unit="g/dL",
        aliases=["hb", "hgb", "haemoglobin", "total hemoglobin", "hemoglobin"],
    ),
    "Fasting Blood Glucose": CanonicalBiomarkerDef(
        canonical_name="Fasting Blood Glucose",
        code_loinc="1558-6",
        code_snomed="36048009",
        category="Metabolic Panel",
        standard_unit="mg/dL",
        aliases=[
            "fbg", "fbs", "fasting glucose", "blood sugar fasting",
            "fasting blood sugar", "glucose fasting", "fasting blood glucose",
            "glucose", "blood glucose", "blood sugar"
        ],
    ),
    "Hemoglobin A1c": CanonicalBiomarkerDef(
        canonical_name="Hemoglobin A1c",
        code_loinc="4548-4",
        code_snomed="43396009",
        category="Metabolic Panel",
        standard_unit="%",
        aliases=["hba1c", "a1c", "glycated hemoglobin", "glycohemoglobin", "hemoglobin a1c"],
    ),
    "Serum Creatinine": CanonicalBiomarkerDef(
        canonical_name="Serum Creatinine",
        code_loinc="2160-0",
        code_snomed="70901006",
        category="Renal Panel",
        standard_unit="mg/dL",
        aliases=["creatinine", "s. creatinine", "s creatinine", "serum creatinine", "cr", "creat"],
    ),
    "Serum Potassium": CanonicalBiomarkerDef(
        canonical_name="Serum Potassium",
        code_loinc="2823-3",
        code_snomed="88262002",
        category="Electrolyte Panel",
        standard_unit="mmol/L",
        aliases=["potassium", "k", "k+", "serum k", "serum potassium", "potassium level"],
    ),
    "Serum Sodium": CanonicalBiomarkerDef(
        canonical_name="Serum Sodium",
        code_loinc="2951-2",
        code_snomed="52627005",
        category="Electrolyte Panel",
        standard_unit="mmol/L",
        aliases=["sodium", "na", "na+", "serum na", "serum sodium", "sodium level"],
    ),
    "Total Cholesterol": CanonicalBiomarkerDef(
        canonical_name="Total Cholesterol",
        code_loinc="2093-3",
        code_snomed="121868005",
        category="Lipid Panel",
        standard_unit="mg/dL",
        aliases=["total cholesterol", "cholesterol", "chol", "total chol", "tc"],
    ),
    "HDL Cholesterol": CanonicalBiomarkerDef(
        canonical_name="HDL Cholesterol",
        code_loinc="2085-9",
        code_snomed="397010006",
        category="Lipid Panel",
        standard_unit="mg/dL",
        aliases=["hdl", "hdl cholesterol", "hdl-c", "high density lipoprotein"],
    ),
    "LDL Cholesterol": CanonicalBiomarkerDef(
        canonical_name="LDL Cholesterol",
        code_loinc="13457-7",
        code_snomed="396994002",
        category="Lipid Panel",
        standard_unit="mg/dL",
        aliases=["ldl", "ldl cholesterol", "ldl-c", "low density lipoprotein"],
    ),
    "Triglycerides": CanonicalBiomarkerDef(
        canonical_name="Triglycerides",
        code_loinc="2571-8",
        code_snomed="14740000",
        category="Lipid Panel",
        standard_unit="mg/dL",
        aliases=["triglycerides", "tg", "trigs", "serum triglycerides"],
    ),
    "Platelet Count": CanonicalBiomarkerDef(
        canonical_name="Platelet Count",
        code_loinc="777-3",
        code_snomed="61594008",
        category="Complete Blood Count",
        standard_unit="10^3/uL",
        aliases=["platelets", "plt", "platelet count", "thrombocytes", "platelet"],
    ),
    "White Blood Cell Count": CanonicalBiomarkerDef(
        canonical_name="White Blood Cell Count",
        code_loinc="6690-2",
        code_snomed="767002",
        category="Complete Blood Count",
        standard_unit="10^3/uL",
        aliases=["wbc", "white blood cells", "total wbc", "leukocytes", "total leukocyte count", "tlc"],
    ),
    "Alanine Aminotransferase": CanonicalBiomarkerDef(
        canonical_name="Alanine Aminotransferase",
        code_loinc="1742-6",
        code_snomed="25959000",
        category="Liver Function Panel",
        standard_unit="U/L",
        aliases=["alt", "sgpt", "alanine aminotransferase", "alanine transaminase"],
    ),
    "Aspartate Aminotransferase": CanonicalBiomarkerDef(
        canonical_name="Aspartate Aminotransferase",
        code_loinc="1920-8",
        code_snomed="36377000",
        category="Liver Function Panel",
        standard_unit="U/L",
        aliases=["ast", "sgot", "aspartate aminotransferase", "aspartate transaminase"],
    ),
}


class AliasResolver:
    """
    Deterministic resolver mapping noisy, extracted text strings to canonical LOINC biomarkers.
    """

    def __init__(self):
        # Build normalized lookup index
        self._lookup: Dict[str, CanonicalBiomarkerDef] = {}
        for _, defn in CANONICAL_BIOMARKERS.items():
            for alias in defn.aliases:
                clean_alias = self._normalize_str(alias)
                self._lookup[clean_alias] = defn
            self._lookup[self._normalize_str(defn.canonical_name)] = defn

    @staticmethod
    def _normalize_str(text: str) -> str:
        return "".join(ch for ch in text.lower() if ch.isalnum() or ch.isspace()).strip()

    def resolve(self, raw_text: str) -> Optional[CanonicalBiomarkerDef]:
        """
        Attempts to find a canonical biomarker matching the extracted text.
        Returns CanonicalBiomarkerDef if matched, or None.
        """
        clean = self._normalize_str(raw_text)
        if not clean:
            return None

        # 1. Exact match
        if clean in self._lookup:
            return self._lookup[clean]

        # 2. Token-level exact match
        tokens = clean.split()
        for token in tokens:
            if token in self._lookup:
                return self._lookup[token]

        # 3. Substring matching (for phrases length >= 4)
        for alias, defn in self._lookup.items():
            if len(alias) >= 4 and alias in clean:
                return defn

        return None
