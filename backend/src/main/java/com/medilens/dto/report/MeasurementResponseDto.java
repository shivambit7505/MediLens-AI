package com.medilens.dto.report;

import com.medilens.model.Measurement;
import com.medilens.model.MeasurementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MeasurementResponseDto(
        UUID id,
        UUID reportId,
        String canonicalName,
        String extractedName,
        String codeLoinc,
        String category,
        String observedValueRaw,
        BigDecimal observedValueNumeric,
        String extractedUnit,
        BigDecimal normalizedValueNumeric,
        String normalizedUnit,
        String referenceIntervalRaw,
        MeasurementStatus status,
        BigDecimal confidence,
        Integer pageNumber,
        String sourceTextSnippet,
        String clinicalCitation,
        LocalDate measurementDate
) {
    public static MeasurementResponseDto fromEntity(Measurement m) {
        String citation = m.getMatchedReferenceRange() != null
                ? m.getMatchedReferenceRange().getSourceCitation()
                : null;
        String category = m.getBiomarker() != null ? m.getBiomarker().getCategory() : "Laboratory";
        String loinc = m.getBiomarker() != null ? m.getBiomarker().getCodeLoinc() : null;

        return new MeasurementResponseDto(
                m.getId(),
                m.getReport().getId(),
                m.getBiomarker().getCanonicalName(),
                m.getExtractedName(),
                loinc,
                category,
                m.getObservedValueRaw(),
                m.getObservedValueNumeric(),
                m.getExtractedUnit(),
                m.getNormalizedValueNumeric(),
                m.getNormalizedUnit(),
                m.getExtractedReferenceText(),
                m.getStatus(),
                m.getConfidence(),
                m.getPageNumber(),
                m.getSourceTextSnippet(),
                citation,
                m.getMeasurementDate()
        );
    }
}
