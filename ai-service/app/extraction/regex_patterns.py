import re
from typing import Optional, Tuple

# Matches numeric observation values, e.g., "14.2", "95", "< 0.01", ">= 100.5"
VALUE_PATTERN = re.compile(
    r"(?P<operator>[<>]=?|≤|≥)?\s*(?P<number>\d+(?:\.\d+)?)(?:\s*(?:x\s*10[\^e]\d+))?",
    re.IGNORECASE,
)

# Matches reference range strings, e.g., "13.8 - 17.2", "70-99", "< 200", "0.74 to 1.35"
RANGE_PATTERN = re.compile(
    r"(?P<low>\d+(?:\.\d+)?)\s*(?:-|–|—|to)\s*(?P<high>\d+(?:\.\d+)?)|(?P<single_op>[<>]=?|≤|≥)\s*(?P<single_val>\d+(?:\.\d+)?)",
    re.IGNORECASE,
)

# Recognized laboratory units
KNOWN_UNITS = [
    "g/dl", "g/l", "mg/dl", "mg/l", "mmol/l", "umol/l", "µmol/l", "micromol/l",
    "%", "meq/l", "10^3/ul", "10^6/ul", "10^9/l", "u/l", "iu/l", "cells/ul",
    "/ul", "/cumm", "fl", "pg", "thous/mcl", "k/ul"
]


def parse_numeric_value(text: str) -> Tuple[Optional[float], Optional[str]]:
    """
    Extracts the floating point value and optional operator from raw string.
    Returns (numeric_value, operator_str).
    """
    if not text:
        return None, None

    match = VALUE_PATTERN.search(text.strip())
    if match:
        val_str = match.group("number")
        op = match.group("operator")
        try:
            return float(val_str), op
        except ValueError:
            return None, None
    return None, None


def extract_reference_range(text: str) -> Tuple[Optional[str], Optional[Tuple[int, int]]]:
    """
    Finds and standardizes reference range text and its character span in the input text.
    """
    if not text:
        return None, None
    match = RANGE_PATTERN.search(text)
    if match:
        return match.group(0).strip(), match.span()
    return None, None


def parse_reference_range_text(text: str) -> Optional[str]:
    """
    Finds and standardizes reference range text from a string snippet.
    """
    ref_text, _ = extract_reference_range(text)
    return ref_text


def extract_unit(text: str) -> Optional[str]:
    """
    Identifies a known laboratory unit from a token string.
    Matches longer unit representations first.
    """
    if not text:
        return None
    clean = text.strip().lower()
    sorted_units = sorted(KNOWN_UNITS, key=lambda x: len(x), reverse=True)
    for u in sorted_units:
        # Match unit with whitespace or string boundary or punctuation boundaries
        pattern = r"(?:^|[\s,;()\[\]])" + re.escape(u) + r"(?:[\s,;()\[\]]|$)"
        if re.search(pattern, clean):
            return u
    return None


def is_numeric_token(tok: str) -> Tuple[bool, Optional[float], Optional[str]]:
    """
    Checks if an isolated token is an observation value (e.g. '14.5', '240', '<0.01').
    Returns (is_numeric, numeric_value, operator).
    Filters out alphanumeric acronyms like 'A1c' or 'B12'.
    """
    if not tok:
        return False, None, None
    t = tok.strip()
    # Check for leading operator
    op = None
    if t.startswith(("<=", ">=", "≤", "≥")):
        op = t[:2]
        t = t[2:].strip()
    elif t.startswith(("<", ">")):
        op = t[:1]
        t = t[1:].strip()

    try:
        val = float(t)
        return True, val, op
    except ValueError:
        return False, None, None
