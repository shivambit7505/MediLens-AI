"""
MediLens AI - RAG Data Transfer Objects (Pydantic Models)
Defines structured input and output schemas for evidence-grounded report explanations.
"""

from typing import List, Optional
from pydantic import BaseModel, Field


class RagBiomarkerInput(BaseModel):
    extracted_name: Optional[str] = None
    canonical_name: str
    observed_value_raw: Optional[str] = None
    normalized_value_numeric: float
    normalized_unit: str
    reference_interval_raw: Optional[str] = None
    status: str  # 'LOW', 'NORMAL', 'HIGH', 'CRITICAL', 'UNKNOWN'


class RagExplanationRequest(BaseModel):
    report_id: Optional[str] = None
    patient_age_years: float = 30.0
    patient_gender: str = "ALL"
    measurements: List[RagBiomarkerInput] = Field(default_factory=list)


class EvidenceSource(BaseModel):
    chunk_id: str
    title: str
    source: str
    category: str


class BiomarkerFindingExplanation(BaseModel):
    canonical_name: str
    observed_value: str
    status: str
    reference_interval: Optional[str] = None
    explanation: str
    clinical_significance: str
    lifestyle_guidance: Optional[str] = None
    sources: List[str] = Field(default_factory=list)


class RagExplanationResponse(BaseModel):
    report_id: Optional[str] = None
    summary: str
    findings: List[BiomarkerFindingExplanation] = Field(default_factory=list)
    questions_for_doctor: List[str] = Field(default_factory=list)
    critical_alert: Optional[str] = None
    disclaimer: str
    cited_sources: List[EvidenceSource] = Field(default_factory=list)
    safety_audit_passed: bool = True
