package com.medilens.dto.trend;

import com.medilens.model.Measurement;
import com.medilens.model.MeasurementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BiomarkerDataPointDto(
        UUID measurementId,
        UUID reportId,
        LocalDate measurementDate,
        Instant recordedAt,
        String observedValueRaw,
        BigDecimal normalizedValue,
        String unit,
        MeasurementStatus status,
        String referenceInterval
) {
    public static BiomarkerDataPointDto fromEntity(Measurement m) {
        return new BiomarkerDataPointDto(
                m.getId(),
                m.getReport().getId(),
                m.getMeasurementDate(),
                m.getCreatedAt(),
                m.getObservedValueRaw(),
                m.getNormalizedValueNumeric(),
                m.getNormalizedUnit(),
                m.getStatus(),
                m.getExtractedReferenceText()
        );
    }
}
