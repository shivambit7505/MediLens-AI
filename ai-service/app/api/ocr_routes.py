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
    target_path = request.storage_path
    resolved_path = None

    if os.path.exists(target_path):
        resolved_path = target_path
    else:
        # Check workspace candidates for relative paths
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        candidates = [
            os.path.abspath(target_path),
            os.path.join(os.getcwd(), target_path),
            os.path.join(base_dir, "backend", target_path),
            os.path.join(base_dir, target_path),
        ]
        for c in candidates:
            if os.path.exists(c):
                resolved_path = c
                break

    if resolved_path:
        try:
            image = Image.open(resolved_path)
            return ocr_engine.process_image(image, page_number=request.page_number)
        except Exception:
            return ocr_engine.fallback_result(page_number=request.page_number)
    else:
        return ocr_engine.fallback_result(page_number=request.page_number)
