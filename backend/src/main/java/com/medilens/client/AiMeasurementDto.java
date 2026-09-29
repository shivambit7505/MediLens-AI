package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AiMeasurementDto(
    @JsonProperty("extracted_name") String extractedName,
    @JsonProperty("canonical_name") String canonicalName,
    @JsonProperty("code_loinc") String codeLoinc,
    @JsonProperty("observed_value_raw") String observedValueRaw,
    @JsonProperty("observed_value_numeric") BigDecimal observedValueNumeric,
    @JsonProperty("extracted_unit") String extractedUnit,
    @JsonProperty("normalized_value_numeric") BigDecimal normalizedValueNumeric,
    @JsonProperty("normalized_unit") String normalizedUnit,
    @JsonProperty("reference_interval_raw") String referenceIntervalRaw,
    @JsonProperty("status") String status,
    @JsonProperty("confidence") BigDecimal confidence,
    @JsonProperty("source_snippet") String sourceSnippet,
    @JsonProperty("matched_range_citation") String matchedRangeCitation
) {}
