"""
MediLens AI - Evidence-Grounded Medical RAG Explanation Service
Orchestrates retrieval of validated clinical guidelines, assembles structured patient
educational explanations, and strictly enforces clinical safety guardrails.
"""

from typing import List, Dict, Any, Set
from app.rag.models import (
    RagExplanationRequest,
    RagExplanationResponse,
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
