package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiRagRequest(
    @JsonProperty("report_id") String reportId,
    @JsonProperty("patient_age_years") double patientAgeYears,
    @JsonProperty("patient_gender") String patientGender,
    @JsonProperty("measurements") List<AiRagBiomarkerInput> measurements
) {
    public record AiRagBiomarkerInput(
        @JsonProperty("extracted_name") String extractedName,
        @JsonProperty("canonical_name") String canonicalName,
        @JsonProperty("observed_value_raw") String observedValueRaw,
        @JsonProperty("normalized_value_numeric") double normalizedValueNumeric,
        @JsonProperty("normalized_unit") String normalizedUnit,
        @JsonProperty("reference_interval_raw") String referenceIntervalRaw,
        @JsonProperty("status") String status
    ) {}
}
