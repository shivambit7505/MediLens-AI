package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiRagResponse(
    @JsonProperty("report_id") String reportId,
    @JsonProperty("summary") String summary,
    @JsonProperty("findings") List<AiFindingExplanation> findings,
    @JsonProperty("questions_for_doctor") List<String> questionsForDoctor,
    @JsonProperty("critical_alert") String criticalAlert,
    @JsonProperty("disclaimer") String disclaimer,
    @JsonProperty("cited_sources") List<AiEvidenceSource> citedSources,
    @JsonProperty("safety_audit_passed") boolean safetyAuditPassed
) {
    public record AiFindingExplanation(
        @JsonProperty("canonical_name") String canonicalName,
        @JsonProperty("observed_value") String observedValue,
        @JsonProperty("status") String status,
        @JsonProperty("reference_interval") String referenceInterval,
        @JsonProperty("explanation") String explanation,
        @JsonProperty("clinical_significance") String clinicalSignificance,
        @JsonProperty("lifestyle_guidance") String lifestyleGuidance,
        @JsonProperty("sources") List<String> sources
    ) {}

    public record AiEvidenceSource(
        @JsonProperty("chunk_id") String chunkId,
        @JsonProperty("title") String title,
        @JsonProperty("source") String source,
        @JsonProperty("category") String category
    ) {}
}
