package com.medilens.dto.trend;

import com.medilens.model.Biomarker;

import java.util.UUID;

public record BiomarkerSummaryDto(
        UUID id,
        String canonicalName,
        String codeLoinc,
        String category,
        String standardUnit,
        String description
) {
    public static BiomarkerSummaryDto fromEntity(Biomarker b) {
        return new BiomarkerSummaryDto(
                b.getId(),
                b.getCanonicalName(),
                b.getCodeLoinc(),
                b.getCategory(),
                b.getStandardUnit(),
                b.getDescription()
        );
    }
}
