package com.medilens.dto.trend;

import java.util.List;
import java.util.UUID;

public record BiomarkerHistoryResponseDto(
        UUID biomarkerId,
        String canonicalName,
        String codeLoinc,
        String standardUnit,
        String category,
        TrendStatisticsDto statistics,
        List<BiomarkerDataPointDto> dataPoints
) {}
