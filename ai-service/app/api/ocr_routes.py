import os
from fastapi import APIRouter, HTTPException, Depends, status
from PIL import Image
from app.ocr.models import OcrProcessRequest, OcrResult
from app.ocr.engine import DualOcrEngine
from app.core.security import verify_internal_api_key

router = APIRouter(
    prefix="/internal/v1/ocr",
    tags=["OCR Pipeline"],
    dependencies=[Depends(verify_internal_api_key)],
)

ocr_engine = DualOcrEngine()


@router.post("/process", response_model=OcrResult)
async def process_report_page(request: OcrProcessRequest):
    """
    Executes OpenCV preprocessing and dual-engine OCR on a report page.
    """
    if not os.path.exists(request.storage_path):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Report file not found at path: {request.storage_path}",
        )

    try:
        image = Image.open(request.storage_path)
        result = ocr_engine.process_image(image, page_number=request.page_number)
        return result
    except Exception as ex:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"OCR processing failed: {str(ex)}",
        )
