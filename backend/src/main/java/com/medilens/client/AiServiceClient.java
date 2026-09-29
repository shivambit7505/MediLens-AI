package com.medilens.client;

public interface AiServiceClient {
    AiOcrResponse processOcrPage(String storagePath, int pageNumber);
    AiExtractionResponse extractAndValidateBiomarkers(String rawOcrText, double patientAgeYears, String patientGender);
}
