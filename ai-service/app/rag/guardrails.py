"""
MediLens AI - Clinical Safety Guardrails & Diagnostic Refusal Engine
Enforces statutory educational disclaimers, citation grounding validation,
emergency red-flag injection, and strict prohibition of prescriptive medical diagnoses.
"""

import re
from typing import List, Dict, Any, Tuple, Set, Optional

MANDATORY_DISCLAIMER = (
    "This information is for educational purposes only and does not constitute "
    "medical advice, diagnosis, or treatment recommendations. Always consult with "
    "a qualified healthcare professional regarding any clinical concerns."
)

CRITICAL_EMERGENCY_BANNER = (
    "🚨 CRITICAL CLINICAL ALERT: One or more biomarkers are in a critical range "
    "requiring urgent medical evaluation. Please contact your physician or the nearest "
    "emergency healthcare facility immediately."
)

# Regex patterns that indicate unauthorized diagnostic assertions
PROHIBITED_DIAGNOSTIC_PATTERNS = [
    re.compile(r"\b(?:you|patient)\s+(?:have|has|are suffering from|suffer from)\s+(?:diabetes|cancer|leukemia|anemia|ckd|cirrhosis|kidney failure|heart failure)\b", re.IGNORECASE),
    re.compile(r"\b(?:i|we)\s+diagnose\s+(?:you|patient)\b", re.IGNORECASE),
    re.compile(r"\b(?:you are|patient is)\s+diagnosed\s+with\b", re.IGNORECASE),
    re.compile(r"\b(?:this\s+confirms\s+that\s+you\s+have|confirms\s+a\s+diagnosis\s+of)\b", re.IGNORECASE),
    re.compile(r"\b(?:take|prescribe|administer)\s+\d+\s*(?:mg|ml|units)\s+of\b", re.IGNORECASE),  # Drug dosing prescription
    re.compile(r"\b(?:stop taking your|discontinue your medication)\b", re.IGNORECASE),
]

# Educational replacements for diagnostic language
SAFE_EDUCATIONAL_REPLACEMENTS = [
    (re.compile(r"\byou have diabetes\b", re.IGNORECASE), "elevated fasting glucose patterns may be associated with impaired glucose regulation or diabetes"),
    (re.compile(r"\byou are diagnosed with ([a-zA-Z\s]+)\b", re.IGNORECASE), r"these findings can be observed in conditions such as \1"),
    (re.compile(r"\bi diagnose you with ([a-zA-Z\s]+)\b", re.IGNORECASE), r"clinical evaluation is recommended to investigate possible \1"),
]


class ClinicalSafetyGuardrail:
    def __init__(self, allowed_chunk_ids: Optional[Set[str]] = None):
        self.allowed_chunk_ids = allowed_chunk_ids or set()

    def check_prohibited_diagnoses(self, text: str) -> List[str]:
        """Scans for prohibited diagnostic language or prescriptive directives."""
        violations = []
        for pattern in PROHIBITED_DIAGNOSTIC_PATTERNS:
            match = pattern.search(text)
            if match:
                violations.append(f"Prohibited diagnostic or prescriptive assertion: '{match.group(0)}'")
        return violations

    def sanitize_diagnostic_claims(self, text: str) -> str:
        """Sanitizes deterministic educational text into compliant clinical language."""
        sanitized = text
        for pattern, replacement in SAFE_EDUCATIONAL_REPLACEMENTS:
            sanitized = pattern.sub(replacement, sanitized)
        return sanitized

    def verify_citations(self, text: str, valid_source_ids: Set[str]) -> Tuple[bool, List[str]]:
        """
        Extracts all '[Source: CHUNK_ID]' references and checks if they exist
        in the retrieved knowledge chunks.
        """
        citation_pattern = re.compile(r"\[Source:\s*([A-Za-z0-9_\-]+)\]")
        found_citations = citation_pattern.findall(text)
        
        invalid_citations = []
        for cit in found_citations:
            if valid_source_ids and cit not in valid_source_ids:
                invalid_citations.append(cit)

        is_valid = len(invalid_citations) == 0
        return is_valid, invalid_citations

    def audit_explanation(
        self,
        explanation_text: str,
        valid_source_ids: Set[str],
        has_critical_findings: bool = False,
    ) -> Tuple[bool, List[str], str]:
        """
        Audits full explanation text against clinical safety rules:
        1. Prohibited diagnosis assertions check & sanitization
        2. Verified citations check
        3. Mandatory disclaimer presence
        4. Emergency banner presence when critical findings exist
        """
        issues: List[str] = []
        sanitized = self.sanitize_diagnostic_claims(explanation_text)

        # Check for remaining prohibited diagnostic claims
        violations = self.check_prohibited_diagnoses(sanitized)
        if violations:
            issues.extend(violations)

        # Check citations
        valid_citations, invalid_cits = self.verify_citations(sanitized, valid_source_ids)
        if not valid_citations:
            issues.append(f"Contains ungrounded or fictitious source citations: {invalid_cits}")

        # Ensure mandatory disclaimer
        if MANDATORY_DISCLAIMER not in sanitized:
            sanitized = f"{sanitized}\n\n{MANDATORY_DISCLAIMER}"

        # Ensure critical banner if critical findings exist
        if has_critical_findings and "CRITICAL CLINICAL ALERT" not in sanitized:
            sanitized = f"{CRITICAL_EMERGENCY_BANNER}\n\n{sanitized}"

        audit_passed = len(issues) == 0
        return audit_passed, issues, sanitized
