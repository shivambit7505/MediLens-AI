"""
MediLens AI - Internal RAG Explanation Routes
Provides authenticated endpoint for Spring Boot backend to retrieve evidence-grounded
educational explanations and clinical safety audits.
"""

from fastapi import APIRouter, Depends
from app.rag.models import (
    RagExplanationRequest,
    RagExplanationResponse,
    RagChatRequest,
    RagChatResponse,
)
from app.rag.rag_service import MedicalRagService
from app.core.security import verify_internal_api_key

router = APIRouter(
    prefix="/internal/v1/rag",
    tags=["Evidence-Grounded RAG"],
    dependencies=[Depends(verify_internal_api_key)],
)

rag_service = MedicalRagService()


@router.post("/explain-report", response_model=RagExplanationResponse)
async def explain_report(request: RagExplanationRequest):
    """
    Generates an evidence-grounded educational explanation of patient lab report findings.
    Strictly verifies citations against clinical guidelines, checks diagnostic guardrails,
    and returns statutory disclaimers and emergency flags.
    """
    return rag_service.generate_explanation(request)


@router.post("/chat", response_model=RagChatResponse)
async def rag_chat(request: RagChatRequest):
    """
    Evidence-grounded conversational medical Q&A with strict safety guardrails
    and clinical literature citations.
    """
    return rag_service.generate_chat_reply(request)
