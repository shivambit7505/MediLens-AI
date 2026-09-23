package com.medilens.model;

public enum ReportStatus {
    UPLOADED,
    VALIDATING,
    PREPROCESSING,
    OCR_PROCESSING,
    EXTRACTING,
    NORMALIZING,
    VALIDATING_VALUES,
    COMPLETED,
    FAILED
}
