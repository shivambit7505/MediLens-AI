package com.medilens.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiServiceClientImpl implements AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClientImpl.class);

    private final RestClient restClient;
    private final String internalApiKey;

    public AiServiceClientImpl(
            @Value("${medilens.ai-service.url:http://localhost:8000}") String aiServiceUrl,
            @Value("${medilens.ai-service.api-key:internal_pre_shared_key_between_backend_and_ai_service}") String internalApiKey,
            RestClient.Builder restClientBuilder
    ) {
        this.internalApiKey = internalApiKey;
        this.restClient = restClientBuilder
                .baseUrl(aiServiceUrl)
                .defaultHeader("X-Internal-API-Key", internalApiKey)
                .build();
    }

    @Override
    public AiOcrResponse processOcrPage(String storagePath, int pageNumber) {
        logger.info("Calling AI Service OCR for page {} at {}", pageNumber, storagePath);
        return restClient.post()
                .uri("/internal/v1/ocr/process")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiOcrRequest(storagePath, pageNumber))
                .retrieve()
                .body(AiOcrResponse.class);
    }

    @Override
    public AiExtractionResponse extractAndValidateBiomarkers(String rawOcrText, double patientAgeYears, String patientGender) {
        logger.info("Calling AI Service biomarker extraction for {} chars of OCR text", rawOcrText.length());
        return restClient.post()
                .uri("/internal/v1/biomarkers/extract-and-validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiExtractionRequest(rawOcrText, patientAgeYears, patientGender))
                .retrieve()
                .body(AiExtractionResponse.class);
    }
}
