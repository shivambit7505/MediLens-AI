from fastapi import FastAPI, Header, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings

app = FastAPI(
    title="MediLens AI - Internal AI Engine",
    description="Deterministic OCR, biomarker extraction, unit normalization, reference validation, and evidence-grounded RAG",
    version="1.0.0",
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", tags=["System"])
async def health_check():
    """Health check endpoint for Docker container and orchestrator."""
    return {
        "status": "ok",
        "service": "medilens-ai-service",
        "environment": settings.ENVIRONMENT,
        "ocr_primary": settings.OCR_ENGINE_PRIMARY,
        "ocr_fallback": settings.OCR_ENGINE_FALLBACK,
    }


from app.api.ocr_routes import router as ocr_router
from app.api.biomarker_routes import router as biomarker_router
from app.api.rag_routes import router as rag_router

app.include_router(ocr_router)
app.include_router(biomarker_router)
app.include_router(rag_router)
