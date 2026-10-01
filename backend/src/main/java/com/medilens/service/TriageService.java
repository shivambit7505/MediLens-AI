package com.medilens.service;

import com.medilens.dto.triage.*;
import com.medilens.model.Measurement;
import com.medilens.model.Report;
import com.medilens.model.TriageRule;
import com.medilens.model.TriageUrgency;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import com.medilens.repository.TriageRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TriageService {

    private static final Logger log = LoggerFactory.getLogger(TriageService.class);

    private final TriageRuleRepository triageRuleRepository;
    private final MeasurementRepository measurementRepository;
    private final ReportRepository reportRepository;

    private static final Set<String> ACUTE_EMERGENCY_SYMPTOMS = Set.of(
            "chest pain", "shortness of breath", "sudden weakness or numbness",
            "severe sudden headache", "uncontrolled bleeding", "loss of consciousness",
            "coughing up blood", "severe allergic reaction", "cyanosis (blue lips/face)"
    );

    private static final Set<String> URGENT_SYMPTOMS = Set.of(
            "high fever (>103°f)", "persistent vomiting", "severe abdominal pain",
            "visual disturbances", "sudden confusion or disorientation",
            "inability to keep liquids down", "prolonged dizziness or syncope"
    );

    public TriageService(TriageRuleRepository triageRuleRepository,
                         MeasurementRepository measurementRepository,
                         ReportRepository reportRepository) {
        this.triageRuleRepository = triageRuleRepository;
        this.measurementRepository = measurementRepository;
        this.reportRepository = reportRepository;
    }

    public List<TriageRuleDto> getAllActiveRules() {
        return triageRuleRepository.findByIsActiveTrue().stream()
                .map(this::mapRuleToDto)
                .toList();
    }

    public TriageEvaluationResponse evaluateTriage(UUID userId, TriageEvaluationRequest request) {
        List<BiomarkerReadingDto> readingsToEvaluate = new ArrayList<>(request.getReadings());

        // If requested and user is present, merge latest measurements from user's most recent completed report
        if (request.isIncludeLatestReportBiomarkers() && userId != null) {
            List<Report> reports = reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
            if (!reports.isEmpty()) {
                Report latestReport = reports.get(0);
                List<Measurement> latestMeasurements = measurementRepository.findByReportIdAndUserId(latestReport.getId(), userId);
                Set<String> alreadyProvidedNames = new HashSet<>();
                for (BiomarkerReadingDto r : readingsToEvaluate) {
                    if (r.getCanonicalName() != null) {
                        alreadyProvidedNames.add(r.getCanonicalName().trim().toLowerCase());
                    }
                }
                for (Measurement m : latestMeasurements) {
                    Double val = m.getNormalizedValueNumeric() != null ? m.getNormalizedValueNumeric().doubleValue() :
                            (m.getObservedValueNumeric() != null ? m.getObservedValueNumeric().doubleValue() : null);
                    String canonName = m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName();
                    String unit = m.getNormalizedUnit() != null ? m.getNormalizedUnit() : m.getExtractedUnit();

                    if (val != null && canonName != null && !alreadyProvidedNames.contains(canonName.toLowerCase())) {
                        readingsToEvaluate.add(new BiomarkerReadingDto(
                                canonName,
                                val,
                                unit
                        ));
                        alreadyProvidedNames.add(canonName.toLowerCase());
                    }
                }
            }
        }

        List<TriageTriggerDto> triggers = new ArrayList<>();
        Set<String> recommendedSpecialties = new LinkedHashSet<>();
        List<TriageRule> activeRules = triageRuleRepository.findByIsActiveTrue();

        // 1. Evaluate Biomarker Readings deterministically
        for (BiomarkerReadingDto reading : readingsToEvaluate) {
            if (reading.getCanonicalName() == null || reading.getValueNumeric() == null) {
                continue;
            }

            for (TriageRule rule : activeRules) {
                if (rule.getBiomarkerCanonicalName().equalsIgnoreCase(reading.getCanonicalName().trim())) {
                    if (evaluateCondition(reading.getValueNumeric(), rule.getComparisonOperator(), rule.getThresholdNumeric())) {
                        triggers.add(new TriageTriggerDto(
                                "BIOMARKER",
                                rule.getBiomarkerCanonicalName(),
                                reading.getValueNumeric(),
                                rule.getComparisonOperator(),
                                rule.getThresholdNumeric(),
                                rule.getUnit() != null ? rule.getUnit() : reading.getUnit(),
                                rule.getUrgencyLevel(),
                                rule.getDeterministicActionInstruction(),
                                String.format("Observed value %.2f %s meets critical rule [%s %s %.2f]",
                                        reading.getValueNumeric(),
                                        reading.getUnit() != null ? reading.getUnit() : "",
                                        rule.getBiomarkerCanonicalName(),
                                        rule.getComparisonOperator(),
                                        rule.getThresholdNumeric())
                        ));

                        mapBiomarkerToSpecialties(rule.getBiomarkerCanonicalName(), recommendedSpecialties);
                    }
                }
            }
        }

        // 2. Evaluate Symptoms deterministically
        if (request.getSymptoms() != null) {
            for (String symptomRaw : request.getSymptoms()) {
                if (symptomRaw == null || symptomRaw.isBlank()) continue;
                String normalized = symptomRaw.trim().toLowerCase();

                boolean isEmergency = ACUTE_EMERGENCY_SYMPTOMS.stream().anyMatch(normalized::contains);
                if (isEmergency) {
                    triggers.add(new TriageTriggerDto(
                            "SYMPTOM",
                            symptomRaw,
                            null,
                            null,
                            null,
                            null,
                            TriageUrgency.EMERGENCY,
                            "Immediate emergency medical evaluation strongly advised. Acute symptom carries significant clinical risk.",
                            "Patient reported red-flag emergency symptom: " + symptomRaw
                    ));
                    if (normalized.contains("chest pain") || normalized.contains("shortness of breath")) {
                        recommendedSpecialties.add("Cardiology");
                        recommendedSpecialties.add("Pulmonology");
                    }
                } else {
                    boolean isUrgent = URGENT_SYMPTOMS.stream().anyMatch(normalized::contains);
                    if (isUrgent) {
                        triggers.add(new TriageTriggerDto(
                                "SYMPTOM",
                                symptomRaw,
                                null,
                                null,
                                null,
                                null,
                                TriageUrgency.URGENT,
                                "Same-day or priority next-day clinical consultation recommended.",
                                "Patient reported urgent clinical symptom: " + symptomRaw
                        ));
                    }
                }
            }
        }

        // 3. Determine Overall Urgency
        TriageUrgency overallUrgency = TriageUrgency.ROUTINE;
        boolean hasEmergency = triggers.stream().anyMatch(t -> t.getUrgencyLevel() == TriageUrgency.EMERGENCY);
        boolean hasUrgent = triggers.stream().anyMatch(t -> t.getUrgencyLevel() == TriageUrgency.URGENT);

        String badgeColor = "emerald";
        String actionDirective;

        if (hasEmergency) {
            overallUrgency = TriageUrgency.EMERGENCY;
            badgeColor = "red";
            actionDirective = "IMMEDIATE EMERGENCY ACTION REQUIRED: One or more critical clinical red-flags detected. Call emergency services (911) or proceed immediately to the nearest emergency department. Do not delay medical attention.";
        } else if (hasUrgent) {
            overallUrgency = TriageUrgency.URGENT;
            badgeColor = "amber";
            actionDirective = "URGENT CLINICAL ATTENTION RECOMMENDED: High-priority findings detected. Contact your physician's on-call triage line or visit an Urgent Care clinic within 12 to 24 hours.";
        } else {
            overallUrgency = TriageUrgency.ROUTINE;
            badgeColor = "emerald";
            actionDirective = "ROUTINE MONITORING: No immediate acute safety red-flags triggered. Continue adherence to your regular healthcare provider follow-ups and prescribed wellness plan.";
            if (recommendedSpecialties.isEmpty()) {
                recommendedSpecialties.add("Internal Medicine");
                recommendedSpecialties.add("Primary Care");
            }
        }

        return new TriageEvaluationResponse(
                overallUrgency,
                hasEmergency,
                badgeColor,
                actionDirective,
                triggers,
                new ArrayList<>(recommendedSpecialties),
                "STATUTORY SAFETY NOTICE: MediLens AI is an academic and clinical research system. This deterministic triage evaluation is an algorithmic safety screen and does not constitute a doctor's medical diagnosis, treatment order, or emergency dispatch."
        );
    }

    private boolean evaluateCondition(double val, String operator, double threshold) {
        return switch (operator.trim()) {
            case ">" -> val > threshold;
            case ">=" -> val >= threshold;
            case "<" -> val < threshold;
            case "<=" -> val <= threshold;
            case "==" -> Math.abs(val - threshold) < 1e-4;
            default -> false;
        };
    }

    private void mapBiomarkerToSpecialties(String biomarker, Set<String> specialties) {
        String lower = biomarker.toLowerCase();
        if (lower.contains("glucose") || lower.contains("a1c")) {
            specialties.add("Endocrinology");
        } else if (lower.contains("creatinine") || lower.contains("potassium") || lower.contains("sodium")) {
            specialties.add("Nephrology");
        } else if (lower.contains("cholesterol") || lower.contains("lipid") || lower.contains("troponin")) {
            specialties.add("Cardiology");
        } else if (lower.contains("hemoglobin") || lower.contains("platelet") || lower.contains("wbc")) {
            specialties.add("Hematology");
        }
    }

    private TriageRuleDto mapRuleToDto(TriageRule rule) {
        return new TriageRuleDto(
                rule.getId(),
                rule.getRuleName(),
                rule.getBiomarkerCanonicalName(),
                rule.getComparisonOperator(),
                rule.getThresholdNumeric(),
                rule.getUnit(),
                rule.getUrgencyLevel(),
                rule.getDeterministicActionInstruction(),
                rule.getDisclaimerText()
        );
    }
}
