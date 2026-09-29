package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiExtractionResponse(
    @JsonProperty("extracted_count") int extractedCount,
    @JsonProperty("measurements") List<AiMeasurementDto> measurements
) {}
