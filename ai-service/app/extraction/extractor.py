from typing import List, Optional
from app.ocr.models import ValidatedMeasurement, ExtractionResponse
from app.normalization.alias_dictionary import AliasResolver
from app.normalization.unit_converter import UnitConverter
from app.validation.range_validator import DeterministicRangeValidator
from app.extraction.spatial_analyzer import SpatialAnalyzer
from app.extraction.regex_patterns import (
    parse_numeric_value,
    extract_unit,
    parse_reference_range_text,
    extract_reference_range,
    is_numeric_token,
)


class BiomarkerExtractor:
    """
    Deterministic biomarker extraction and validation engine.
    Extracts tabular measurements, resolves canonical vocabulary, standardizes units,
    and applies clinical reference ranges.
    Never relies on an LLM for factual medical values.
    """

    def __init__(self):
        self.spatial_analyzer = SpatialAnalyzer()
        self.alias_resolver = AliasResolver()
        self.unit_converter = UnitConverter()
        self.range_validator = DeterministicRangeValidator()

    def extract_from_text(
        self,
        raw_text: str,
        patient_age_years: float = 30.0,
        patient_gender: str = "ALL",
        confidence_score: float = 0.95,
    ) -> ExtractionResponse:
        """
        Parses raw OCR text lines, identifies biomarker test rows, and produces
        deterministically validated measurements.
        """
        lines = self.spatial_analyzer.parse_plain_text_lines(raw_text)
        measurements: List[ValidatedMeasurement] = []

        for line in lines:
            measurement = self._process_text_line(
                line, patient_age_years, patient_gender, confidence_score
            )
            if measurement:
                measurements.append(measurement)

        return ExtractionResponse(
            extracted_count=len(measurements),
            measurements=measurements,
        )

    def _process_text_line(
        self,
        line: str,
        patient_age_years: float,
        patient_gender: str,
        confidence_score: float,
    ) -> Optional[ValidatedMeasurement]:
        """
        Inspects an individual line to check if it matches a canonical laboratory biomarker.
        """
        tokens = line.split()
        if len(tokens) < 2:
            return None

        # 1. First isolate reference range string so its numbers are not confused with observed values
        ref_text, ref_span = extract_reference_range(line)
        if ref_span:
            line_without_ref = line[:ref_span[0]] + " " + line[ref_span[1]:]
        else:
            line_without_ref = line

        tokens_clean = line_without_ref.split()
        if not tokens_clean:
            return None

        # 2. Find the first token that is an observation value
        val_idx = None
        num_val = None
        operator = None

        for idx, tok in enumerate(tokens_clean):
            is_num, val, op = is_numeric_token(tok)
            if is_num:
                val_idx = idx
                num_val = val
                operator = op
                break

        if val_idx is None or num_val is None:
            return None

        # 3. Tokens before val_idx form the candidate biomarker name
        if val_idx == 0:
            return None

        candidate_name = " ".join(tokens_clean[:val_idx])
        matched_biomarker = self.alias_resolver.resolve(candidate_name)
        if not matched_biomarker:
            for start_idx in range(1, val_idx):
                sub_candidate = " ".join(tokens_clean[start_idx:val_idx])
                matched = self.alias_resolver.resolve(sub_candidate)
                if matched:
                    matched_biomarker = matched
                    candidate_name = sub_candidate
                    break

        if not matched_biomarker:
            return None

        # 4. Extract unit from tokens following val_idx or from the line
        after_val = " ".join(tokens_clean[val_idx + 1:])
        extracted_unit = extract_unit(after_val)

        # 5. Deterministic Unit Normalization
        norm_val, norm_unit, converted = self.unit_converter.convert(
            canonical_name=matched_biomarker.canonical_name,
            value=num_val,
            from_unit=extracted_unit,
            standard_unit=matched_biomarker.standard_unit,
        )

        # 6. Deterministic Reference Range Validation
        status, matched_rule = self.range_validator.validate(
            canonical_name=matched_biomarker.canonical_name,
            normalized_value=norm_val,
            normalized_unit=norm_unit,
            patient_age_years=patient_age_years,
            patient_gender=patient_gender,
            conversion_successful=converted,
        )

        citation = matched_rule.source_citation if matched_rule else None

        return ValidatedMeasurement(
            extracted_name=candidate_name,
            canonical_name=matched_biomarker.canonical_name,
            code_loinc=matched_biomarker.code_loinc,
            observed_value_raw=str(num_val),
            observed_value_numeric=num_val,
            extracted_unit=extracted_unit,
            normalized_value_numeric=norm_val,
            normalized_unit=norm_unit,
            reference_interval_raw=ref_text,
            status=status,
            confidence=confidence_score,
            source_snippet=line.strip(),
            matched_range_citation=citation,
        )
