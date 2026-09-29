package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiOcrResponse(
    @JsonProperty("page_number") int pageNumber,
    @JsonProperty("raw_text") String rawText,
    @JsonProperty("confidence_score") double confidenceScore,
    @JsonProperty("engine_used") String engineUsed
) {}
