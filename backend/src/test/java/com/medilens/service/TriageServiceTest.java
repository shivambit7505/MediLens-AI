package com.medilens.service;

import com.medilens.dto.triage.BiomarkerReadingDto;
import com.medilens.dto.triage.TriageEvaluationRequest;
import com.medilens.dto.triage.TriageEvaluationResponse;
import com.medilens.model.TriageRule;
import com.medilens.model.TriageUrgency;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import com.medilens.repository.TriageRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock
    private TriageRuleRepository triageRuleRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private TriageService triageService;

    private List<TriageRule> defaultRules;

    @BeforeEach
    void setUp() {
        defaultRules = List.of(
                new TriageRule(
                        UUID.randomUUID(), "Critical Hyperkalemia", "Serum Potassium",
                        ">=", 6.5, "mmol/L", TriageUrgency.EMERGENCY,
                        "Proceed immediately to emergency department.",
                        "Safety rule", true, Instant.now()
                ),
                new TriageRule(
                        UUID.randomUUID(), "Severe Hypoglycemia", "Fasting Blood Glucose",
                        "<=", 50.0, "mg/dL", TriageUrgency.EMERGENCY,
                        "Administer fast-acting carbohydrates.",
                        "Safety rule", true, Instant.now()
                ),
                new TriageRule(
                        UUID.randomUUID(), "Stage 3 AKI", "Serum Creatinine",
                        ">=", 4.0, "mg/dL", TriageUrgency.URGENT,
                        "Urgent nephrology consultation.",
                        "Safety rule", true, Instant.now()
                )
        );
    }

    @Test
    @DisplayName("Should detect EMERGENCY for critical hyperkalemia (Potassium >= 6.5)")
    void testCriticalHyperkalemiaTrigger() {
        when(triageRuleRepository.findByIsActiveTrue()).thenReturn(defaultRules);

        TriageEvaluationRequest request = new TriageEvaluationRequest(
                List.of(new BiomarkerReadingDto("Serum Potassium", 6.8, "mmol/L")),
                Collections.emptyList(),
                false
        );

        TriageEvaluationResponse response = triageService.evaluateTriage(null, request);

        assertThat(response.getOverallUrgency()).isEqualTo(TriageUrgency.EMERGENCY);
        assertThat(response.isEmergencyFlag()).isTrue();
        assertThat(response.getUrgencyBadgeColor()).isEqualTo("red");
        assertThat(response.getTriggers()).hasSize(1);
        assertThat(response.getTriggers().get(0).getName()).isEqualTo("Serum Potassium");
        assertThat(response.getRecommendedSpecialties()).contains("Nephrology");
    }

    @Test
    @DisplayName("Should detect EMERGENCY when acute symptom 'chest pain' is reported")
    void testAcuteSymptomEmergency() {
        when(triageRuleRepository.findByIsActiveTrue()).thenReturn(defaultRules);

        TriageEvaluationRequest request = new TriageEvaluationRequest(
                List.of(new BiomarkerReadingDto("Serum Potassium", 4.2, "mmol/L")), // normal potassium
                List.of("Severe chest pain radiating to left arm"),
                false
        );

        TriageEvaluationResponse response = triageService.evaluateTriage(null, request);

        assertThat(response.getOverallUrgency()).isEqualTo(TriageUrgency.EMERGENCY);
        assertThat(response.isEmergencyFlag()).isTrue();
        assertThat(response.getRecommendedSpecialties()).contains("Cardiology");
    }

    @Test
    @DisplayName("Should return ROUTINE when all readings and symptoms are benign")
    void testRoutineBenignEvaluation() {
        when(triageRuleRepository.findByIsActiveTrue()).thenReturn(defaultRules);

        TriageEvaluationRequest request = new TriageEvaluationRequest(
                List.of(
                        new BiomarkerReadingDto("Serum Potassium", 4.1, "mmol/L"),
                        new BiomarkerReadingDto("Fasting Blood Glucose", 88.0, "mg/dL"),
                        new BiomarkerReadingDto("Serum Creatinine", 0.9, "mg/dL")
                ),
                Collections.emptyList(),
                false
        );

        TriageEvaluationResponse response = triageService.evaluateTriage(null, request);

        assertThat(response.getOverallUrgency()).isEqualTo(TriageUrgency.ROUTINE);
        assertThat(response.isEmergencyFlag()).isFalse();
        assertThat(response.getUrgencyBadgeColor()).isEqualTo("emerald");
        assertThat(response.getTriggers()).isEmpty();
    }
}
