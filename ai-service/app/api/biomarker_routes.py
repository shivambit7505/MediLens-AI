from fastapi import APIRouter, Depends
from app.ocr.models import ExtractionRequest, ExtractionResponse
from app.extraction.extractor import BiomarkerExtractor
from app.core.security import verify_internal_api_key

router = APIRouter(
    prefix="/internal/v1/biomarkers",
    tags=["Biomarker Extraction"],
    dependencies=[Depends(verify_internal_api_key)],
)

extractor = BiomarkerExtractor()


@router.post("/extract-and-validate", response_model=ExtractionResponse)
async def extract_and_validate_biomarkers(request: ExtractionRequest):
    """
    Deterministically extracts biomarkers, normalizes units/aliases,
    and validates observed values against age/gender reference ranges.
    Never relies on an LLM for medical facts.
    """
    result = extractor.extract_from_text(
        raw_text=request.raw_ocr_text,
        patient_age_years=request.patient_age_years,
        patient_gender=request.patient_gender,
    )
    return result
