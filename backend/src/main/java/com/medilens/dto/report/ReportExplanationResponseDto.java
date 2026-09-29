package com.medilens.dto.report;

import java.util.List;
import java.util.UUID;

public record ReportExplanationResponseDto(
    UUID reportId,
    String summary,
    List<BiomarkerExplanationDto> findings,
    List<String> questionsForDoctor,
    String criticalAlert,
    String disclaimer,
    List<EvidenceSourceDto> citedSources,
    boolean safetyAuditPassed
) {
    public record BiomarkerExplanationDto(
        String canonicalName,
        String observedValue,
        String status,
        String referenceInterval,
        String explanation,
        String clinicalSignificance,
        String lifestyleGuidance,
        List<String> sources
    ) {}

    public record EvidenceSourceDto(
        String chunkId,
        String title,
        String source,
        String category
    ) {}
}
