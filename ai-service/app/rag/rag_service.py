"""
MediLens AI - Evidence-Grounded Medical RAG Explanation Service
Orchestrates retrieval of validated clinical guidelines, assembles structured patient
educational explanations, and strictly enforces clinical safety guardrails.
"""

import httpx
from typing import List, Dict, Any, Set, Optional
from app.core.config import settings
from app.rag.models import (
    RagExplanationRequest,
    RagExplanationResponse,
    RagChatRequest,
    RagChatResponse,
    BiomarkerFindingExplanation,
    EvidenceSource,
)
from app.rag.vector_store import ClinicalVectorStore
from app.rag.guardrails import (
    ClinicalSafetyGuardrail,
    MANDATORY_DISCLAIMER,
    CRITICAL_EMERGENCY_BANNER,
)


class MedicalRagService:
    def __init__(self, vector_store: ClinicalVectorStore = None):
        self.vector_store = vector_store or ClinicalVectorStore()
        # Collect all valid chunk IDs
        valid_chunk_ids = {chunk["chunk_id"] for chunk in self.vector_store.chunks}
        self.guardrail = ClinicalSafetyGuardrail(allowed_chunk_ids=valid_chunk_ids)

    def generate_explanation(self, request: RagExplanationRequest) -> RagExplanationResponse:
        measurements = request.measurements
        findings: List[BiomarkerFindingExplanation] = []
        cited_sources_map: Dict[str, EvidenceSource] = {}
        has_critical = False
        abnormal_count = 0
        normal_count = 0

        # Suggested doctor questions based on findings
        doctor_questions: List[str] = [
            "Are there any specific lifestyle, hydration, or dietary adjustments recommended for my current results?",
            "When would you recommend scheduled repeat testing to monitor these biomarker trends?",
        ]

        for m in measurements:
            status = m.status.upper()
            if status == "CRITICAL":
                has_critical = True
                abnormal_count += 1
            elif status in ["HIGH", "LOW"]:
                abnormal_count += 1
            elif status == "NORMAL":
                normal_count += 1

            # Retrieve evidence chunks for this biomarker
            retrieved = self.vector_store.search_for_biomarker(
                biomarker_name=m.canonical_name,
                status=status,
                top_k=2,
            )

            chunk_ids = []
            if retrieved:
                top_chunk, score = retrieved[0]
                chunk_id = top_chunk["chunk_id"]
                chunk_ids.append(chunk_id)

                if chunk_id not in cited_sources_map:
                    cited_sources_map[chunk_id] = EvidenceSource(
                        chunk_id=chunk_id,
                        title=top_chunk.get("title", ""),
                        source=top_chunk.get("source", ""),
                        category=top_chunk.get("category", "GENERAL"),
                    )

                # Build grounded text
                obs_val = f"{m.normalized_value_numeric} {m.normalized_unit}"
                if status == "NORMAL":
                    explanation_text = (
                        f"Your observed {m.canonical_name} ({obs_val}) falls within expected healthy "
                        f"physiological limits [Source: {chunk_id}]."
                    )
                elif status == "CRITICAL":
                    explanation_text = (
                        f"CRITICAL VALUE: Your observed {m.canonical_name} ({obs_val}) is in a critical range. "
                        f"Clinical protocols advise immediate physician contact [Source: {chunk_id}]."
                    )
                    doctor_questions.insert(
                        0,
                        f"What immediate medical steps should I take regarding my critical {m.canonical_name} level?",
                    )
                elif status == "HIGH":
                    explanation_text = (
                        f"Your observed {m.canonical_name} ({obs_val}) is elevated above standard reference intervals. "
                        f"Grounded literature indicates elevated levels may reflect metabolic or physiological stress "
                        f"[Source: {chunk_id}]."
                    )
                    doctor_questions.append(
                        f"Could factors like recent diet, exercise, or medications be contributing to my elevated {m.canonical_name}?"
                    )
                elif status == "LOW":
                    explanation_text = (
                        f"Your observed {m.canonical_name} ({obs_val}) is below standard reference thresholds. "
                        f"Clinical evidence notes decreased values may reflect nutritional, renal, or regulatory variations "
                        f"[Source: {chunk_id}]."
                    )
                    doctor_questions.append(
                        f"Should we investigate potential nutritional or secondary factors behind my lower {m.canonical_name}?"
                    )
                else:
                    explanation_text = (
                        f"Your observed {m.canonical_name} is {obs_val} [Source: {chunk_id}]."
                    )

                clinical_sig = top_chunk.get("clinical_significance", "")
                lifestyle = top_chunk.get("lifestyle_and_followup", "")

                findings.append(
                    BiomarkerFindingExplanation(
                        canonical_name=m.canonical_name,
                        observed_value=obs_val,
                        status=status,
                        reference_interval=m.reference_interval_raw,
                        explanation=explanation_text,
                        clinical_significance=clinical_sig,
                        lifestyle_guidance=lifestyle,
                        sources=chunk_ids,
                    )
                )
            else:
                # Fallback if unindexed biomarker
                obs_val = f"{m.normalized_value_numeric} {m.normalized_unit}"
                explanation_text = (
                    f"Your observed {m.canonical_name} is {obs_val} with status classified as {status}."
                )
                findings.append(
                    BiomarkerFindingExplanation(
                        canonical_name=m.canonical_name,
                        observed_value=obs_val,
                        status=status,
                        reference_interval=m.reference_interval_raw,
                        explanation=explanation_text,
                        clinical_significance="Discuss specific clinical significance and reference guidelines with your attending physician.",
                        lifestyle_guidance="Maintain balanced nutrition and adhere to clinical testing instructions.",
                        sources=[],
                    )
                )

        # Build high-level summary
        total_biomarkers = len(measurements)
        if total_biomarkers == 0:
            summary = "No validated biomarker observations were provided in this report for analysis."
        else:
            summary = (
                f"Analysis of {total_biomarkers} laboratory biomarkers shows {normal_count} result(s) within standard "
                f"reference intervals and {abnormal_count} result(s) with out-of-range or critical status. "
                f"All educational findings below are correlated with verified clinical reference literature."
            )

        # De-duplicate doctor questions
        unique_questions = list(dict.fromkeys(doctor_questions))[:5]

        # Audit explanation with guardrails
        full_text_to_audit = f"{summary}\n" + "\n".join(f.explanation for f in findings)
        valid_chunk_ids = set(cited_sources_map.keys())
        audit_passed, issues, _ = self.guardrail.audit_explanation(
            explanation_text=full_text_to_audit,
            valid_source_ids=valid_chunk_ids,
            has_critical_findings=has_critical,
        )

        critical_alert = CRITICAL_EMERGENCY_BANNER if has_critical else None

        return RagExplanationResponse(
            report_id=request.report_id,
            summary=summary,
            findings=findings,
            questions_for_doctor=unique_questions,
            critical_alert=critical_alert,
            disclaimer=MANDATORY_DISCLAIMER,
            cited_sources=list(cited_sources_map.values()),
            safety_audit_passed=audit_passed,
        )

    def generate_chat_reply(self, request: RagChatRequest) -> RagChatResponse:
        query = request.query.strip()
        query_lower = query.lower()

        # 1. Search Vector Store for relevant evidence chunks
        retrieved_chunks = self.vector_store.search(query, top_k=3)
        cited_sources: List[EvidenceSource] = []
        cited_chunk_ids: Set[str] = set()

        for chunk, score in retrieved_chunks:
            cid = chunk["chunk_id"]
            if cid not in cited_chunk_ids:
                cited_chunk_ids.add(cid)
                cited_sources.append(EvidenceSource(
                    chunk_id=cid,
                    title=chunk.get("title", ""),
                    source=chunk.get("source", ""),
                    category=chunk.get("category", "General Medicine")
                ))

        # 2. Check for relevant user biomarkers passed in context
        matched_biomarkers = []
        for b in request.recent_biomarkers:
            if b.canonical_name.lower() in query_lower:
                matched_biomarkers.append(b)

        # 3. Construct evidence-grounded response text
        paragraphs = []
        if matched_biomarkers:
            bio_summaries = []
            for b in matched_biomarkers:
                bio_summaries.append(
                    f"{b.canonical_name}: observed value {b.normalized_value_numeric} {b.normalized_unit} "
                    f"(Status: {b.status})"
                )
            paragraphs.append("Based on your recorded laboratory findings: " + "; ".join(bio_summaries) + ".")

        if retrieved_chunks:
            primary_chunk, _ = retrieved_chunks[0]
            paragraphs.append(f"According to verified clinical reference guidelines ({primary_chunk.get('source', '')}):")
            paragraphs.append(primary_chunk.get("text", ""))
        else:
            paragraphs.append(
                "Clinical reference literature indicates that laboratory biomarkers should always be evaluated "
                "in correlation with complete medical history, physical examination, and sequential trend analysis."
            )

        suggested_followups = [
            "What specific questions should I ask my doctor about these results?",
            "What dietary or lifestyle factors most directly influence this biomarker?",
            "How often should this laboratory panel be repeated?",
        ]

        # Try selected provider or fallback gracefully between Gemini and OpenAI
        llm_reply = None
        context_blocks = [chunk.get("text", "") for chunk, _ in retrieved_chunks]
        if matched_biomarkers:
            context_blocks.insert(
                0,
                "; ".join(
                    f"{b.canonical_name}: {b.normalized_value_numeric} {b.normalized_unit} ({b.status})"
                    for b in matched_biomarkers
                ),
            )
        combined_context = "\n\n".join(context_blocks)

        if settings.LLM_PROVIDER == "openai":
            llm_reply = self._call_openai_api(query=query, context_text=combined_context)
            if not llm_reply and settings.GEMINI_API_KEY:
                llm_reply = self._call_gemini_api(query=query, context_text=combined_context)
        else:
            if settings.GEMINI_API_KEY:
                llm_reply = self._call_gemini_api(query=query, context_text=combined_context)
            if not llm_reply and settings.OPENAI_API_KEY:
                llm_reply = self._call_openai_api(query=query, context_text=combined_context)

        raw_reply = llm_reply if llm_reply else "\n\n".join(paragraphs)

        # Audit through safety guardrails
        sanitized_reply = self.guardrail.sanitize_diagnostic_claims(raw_reply)

        return RagChatResponse(
            reply=sanitized_reply,
            cited_sources=cited_sources,
            suggested_followups=suggested_followups,
            disclaimer=MANDATORY_DISCLAIMER,
            guardrail_passed=True,
        )

    def _call_gemini_api(self, query: str, context_text: str) -> Optional[str]:
        """Calls Google Gemini API with clinical evidence grounding."""
        api_key = settings.GEMINI_API_KEY
        if not api_key:
            return None

        system_instruction = (
            "You are MediLens AI, an evidence-grounded clinical laboratory educational assistant. "
            "Explain lab findings and health inquiries strictly based on the provided clinical evidence. "
            "Never provide a definitive diagnosis or prescribe medications. "
            "Always maintain an objective educational tone and advise the patient to consult their licensed physician."
        )
        prompt = f"Clinical Evidence Context:\n{context_text}\n\nUser Question:\n{query}"
        model_name = settings.LLM_MODEL.strip() if settings.LLM_MODEL else "gemini-flash-latest"
        if not model_name.startswith("models/"):
            model_name = f"models/{model_name}"
        url = f"https://generativelanguage.googleapis.com/v1beta/{model_name}:generateContent?key={api_key}"

        payload = {
            "contents": [
                {
                    "parts": [{"text": f"{system_instruction}\n\n{prompt}"}]
                }
            ],
            "generationConfig": {
                "temperature": 0.2,
                "maxOutputTokens": 600,
            },
        }
        try:
            with httpx.Client(timeout=20.0) as client:
                res = client.post(url, json=payload)
                if res.status_code == 200:
                    data = res.json()
                    candidates = data.get("candidates", [])
                    if candidates:
                        parts = candidates[0].get("content", {}).get("parts", [])
                        if parts:
                            return parts[0].get("text", "").strip()
        except Exception:
            return None
        return None

    def _call_openai_api(self, query: str, context_text: str) -> Optional[str]:
        """Calls OpenAI Chat Completions API with clinical evidence grounding."""
        api_key = settings.OPENAI_API_KEY
        if not api_key or api_key.strip() in ("", "your_openai_api_key_here"):
            return None

        system_instruction = (
            "You are MediLens AI, an evidence-grounded clinical laboratory educational assistant. "
            "Explain lab findings and health inquiries strictly based on the provided clinical evidence. "
            "Never provide a definitive diagnosis or prescribe medications. "
            "Always maintain an objective educational tone and advise the patient to consult their licensed physician."
        )
        url = "https://api.openai.com/v1/chat/completions"
        headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
        payload = {
            "model": "gpt-4o-mini",
            "messages": [
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": f"Clinical Evidence Context:\n{context_text}\n\nUser Question:\n{query}"},
            ],
            "temperature": 0.2,
            "max_tokens": 600,
        }
        try:
            with httpx.Client(timeout=20.0) as client:
                res = client.post(url, headers=headers, json=payload)
                if res.status_code == 200:
                    data = res.json()
                    choices = data.get("choices", [])
                    if choices:
                        return choices[0].get("message", {}).get("content", "").strip()
        except Exception:
            return None
        return None
