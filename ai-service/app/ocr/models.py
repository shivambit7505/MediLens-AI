from typing import List, Optional
from pydantic import BaseModel, Field


class BoundingBox(BaseModel):
    x: int
    y: int
    width: int
    height: int


class OcrToken(BaseModel):
    text: str
    confidence: float
    bounding_box: Optional[BoundingBox] = None


class OcrLine(BaseModel):
    text: str
    confidence: float
    bounding_box: Optional[BoundingBox] = None
    tokens: List[OcrToken] = Field(default_factory=list)


class OcrResult(BaseModel):
    page_number: int = 1
    engine_used: str  # 'paddleocr' or 'tesseract'
    mean_confidence: float
    raw_text: str
    lines: List[OcrLine] = Field(default_factory=list)


class OcrProcessRequest(BaseModel):
    storage_path: str
    page_number: int = 1


class ExtractionRequest(BaseModel):
    raw_ocr_text: str
    patient_age_years: float = 30.0
    patient_gender: str = "ALL"  # 'MALE', 'FEMALE', 'OTHER', 'ALL'


class ValidatedMeasurement(BaseModel):
    extracted_name: str
    canonical_name: str
    code_loinc: Optional[str] = None
    observed_value_raw: str
    observed_value_numeric: Optional[float] = None
    extracted_unit: Optional[str] = None
    normalized_value_numeric: float
    normalized_unit: str
    reference_interval_raw: Optional[str] = None
    status: str  # 'LOW', 'NORMAL', 'HIGH', 'CRITICAL', 'UNKNOWN'
    confidence: float
    source_snippet: str
    matched_range_citation: Optional[str] = None


class ExtractionResponse(BaseModel):
    extracted_count: int
    measurements: List[ValidatedMeasurement] = Field(default_factory=list)
