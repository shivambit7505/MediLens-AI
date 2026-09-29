package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiExtractionRequest(
    @JsonProperty("raw_ocr_text") String rawOcrText,
    @JsonProperty("patient_age_years") double patientAgeYears,
    @JsonProperty("patient_gender") String patientGender
) {}
