package com.medilens.service;

import com.medilens.dto.provider.CareNavigationResponseDto;
import com.medilens.dto.provider.ProviderDto;
import com.medilens.dto.provider.SpecialtyRecommendationDto;
import com.medilens.model.Measurement;
import com.medilens.model.MeasurementStatus;
import com.medilens.model.Report;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorFinderService {

    private final ReportRepository reportRepository;
    private final MeasurementRepository measurementRepository;

    private static final List<ProviderDto> PROVIDER_DIRECTORY = new ArrayList<>();

    static {
        PROVIDER_DIRECTORY.add(new ProviderDto(
                UUID.fromString("d0000000-0000-0000-0000-000000000001"),
                "Dr. Elena Vance, MD, FACP",
                "Board Certified Endocrinologist",
                "Endocrinology",
                "Metabolic & Diabetes Specialty Center",
                "742 Evergreen Terrace, Suite 300",
                "Springfield", "IL", "62704",
                "(555) 234-8901",
                4.9, 142, 2.4,
                true, true,
                List.of("Springfield Memorial Hospital", "University Medical Center"),
                List.of("Type 1 & 2 Diabetes", "Insulin Resistance", "Thyroid Disorders", "Metabolic Syndrome")
        ));

        PROVIDER_DIRECTORY.add(new ProviderDto(
                UUID.fromString("d0000000-0000-0000-0000-000000000002"),
                "Dr. Marcus Brody, MD, FACC",
                "Interventional Cardiologist",
                "Cardiology",
                "Apex Heart & Vascular Institute",
                "100 Medical Parkway, Bldg B",
                "Springfield", "IL", "62702",
                "(555) 345-6789",
                4.8, 98, 3.8,
                true, true,
                List.of("Springfield Memorial Hospital", "St. Jude Heart Institute"),
                List.of("Lipid Management", "Coronary Artery Disease", "Hypertension", "Arrhythmias")
        ));

        PROVIDER_DIRECTORY.add(new ProviderDto(
                UUID.fromString("d0000000-0000-0000-0000-000000000003"),
                "Dr. Ananya Patel, MD",
                "Specialist Nephrologist",
                "Nephrology",
                "Midwest Kidney Care Associates",
                "520 Lincoln Ave, Suite 104",
                "Springfield", "IL", "62701",
                "(555) 456-7890",
                4.9, 87, 4.1,
                false, true,
                List.of("Springfield Memorial Hospital"),
                List.of("Chronic Kidney Disease (CKD)", "Electrolyte Disorders", "Glomerulonephritis", "Renal Hypertension")
        ));

        PROVIDER_DIRECTORY.add(new ProviderDto(
                UUID.fromString("d0000000-0000-0000-0000-000000000004"),
                "Dr. David Kim, MD, PhD",
                "Clinical Hematologist",
                "Hematology",
                "Comprehensive Blood Health Center",
                "890 Oak Ridge Blvd",
                "Springfield", "IL", "62703",
                "(555) 567-8901",
                4.7, 65, 5.0,
                true, false,
                List.of("University Medical Center"),
                List.of("Microcytic Anemia", "Thrombocytopenia", "Coagulation Disorders", "Leukopenia")
        ));

        PROVIDER_DIRECTORY.add(new ProviderDto(
                UUID.fromString("d0000000-0000-0000-0000-000000000005"),
                "Dr. Sarah Jenkins, MD",
                "Primary Care & Preventive Medicine Physician",
                "Internal Medicine",
                "City Health Primary Care Group",
                "210 Main Street",
                "Springfield", "IL", "62701",
                "(555) 678-9012",
                4.9, 215, 1.2,
                true, true,
                List.of("Springfield Memorial Hospital"),
                List.of("Comprehensive Health Screenings", "Chronic Disease Navigation", "Preventive Wellness")
        ));
    }

    public DoctorFinderService(ReportRepository reportRepository, MeasurementRepository measurementRepository) {
        this.reportRepository = reportRepository;
        this.measurementRepository = measurementRepository;
    }

    public CareNavigationResponseDto getCareNavigationForUser(UUID userId) {
        List<SpecialtyRecommendationDto> recommendations = new ArrayList<>();

        if (userId != null) {
            List<Report> reports = reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
            if (!reports.isEmpty()) {
                Report latest = reports.get(0);
                List<Measurement> measurements = measurementRepository.findByReportIdAndUserId(latest.getId(), userId);

                Map<String, List<Measurement>> abnormalBySystem = new HashMap<>();

                for (Measurement m : measurements) {
                    if (m.getStatus() == MeasurementStatus.HIGH ||
                        m.getStatus() == MeasurementStatus.LOW ||
                        m.getStatus() == MeasurementStatus.CRITICAL) {

                        String category = m.getBiomarker() != null ? m.getBiomarker().getCategory() : "General";
                        String canonName = m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName();
                        String system = mapCategoryToSystem(category, canonName);
                        abnormalBySystem.computeIfAbsent(system, k -> new ArrayList<>()).add(m);
                    }
                }

                for (Map.Entry<String, List<Measurement>> entry : abnormalBySystem.entrySet()) {
                    recommendations.add(buildSpecialtyRecommendation(entry.getKey(), entry.getValue()));
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(new SpecialtyRecommendationDto(
                    "Internal Medicine",
                    "Routine general health maintenance and ongoing clinical observation.",
                    "ROUTINE",
                    Collections.emptyList(),
                    "Discuss annual wellness checkup, lifestyle optimizations, and routine preventive blood work scheduling."
            ));
        }

        return new CareNavigationResponseDto(
                recommendations,
                PROVIDER_DIRECTORY,
                "DISCLAIMER: MediLens AI care navigation suggestions and provider listings are for educational routing and informational purposes. MediLens AI does not endorse specific clinicians or receive referral fees."
        );
    }

    public List<ProviderDto> searchProviders(String specialty, String query, Boolean telehealthOnly) {
        return PROVIDER_DIRECTORY.stream()
                .filter(p -> {
                    if (specialty != null && !specialty.isBlank() && !specialty.equalsIgnoreCase("ALL")) {
                        if (!p.getSpecialty().equalsIgnoreCase(specialty.trim())) return false;
                    }
                    if (query != null && !query.isBlank()) {
                        String q = query.trim().toLowerCase();
                        boolean matchesName = p.getName().toLowerCase().contains(q);
                        boolean matchesClinic = p.getClinicName().toLowerCase().contains(q);
                        boolean matchesCity = p.getCity().toLowerCase().contains(q);
                        if (!matchesName && !matchesClinic && !matchesCity) return false;
                    }
                    if (telehealthOnly != null && telehealthOnly) {
                        if (!p.isTelehealthAvailable()) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private String mapCategoryToSystem(String category, String biomarker) {
        String b = biomarker.toLowerCase();
        if (b.contains("glucose") || b.contains("a1c")) return "Endocrinology";
        if (b.contains("creatinine") || b.contains("potassium") || b.contains("sodium") || b.contains("bun")) return "Nephrology";
        if (b.contains("cholesterol") || b.contains("lipid") || b.contains("triglyceride") || b.contains("troponin")) return "Cardiology";
        if (b.contains("hemoglobin") || b.contains("platelet") || b.contains("wbc") || b.contains("rbc")) return "Hematology";
        return "Internal Medicine";
    }

    private SpecialtyRecommendationDto buildSpecialtyRecommendation(String specialty, List<Measurement> abnormalMeds) {
        List<String> names = abnormalMeds.stream()
                .map(m -> {
                    String bName = m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName();
                    Double val = m.getNormalizedValueNumeric() != null ? m.getNormalizedValueNumeric().doubleValue() :
                            (m.getObservedValueNumeric() != null ? m.getObservedValueNumeric().doubleValue() : null);
                    String u = m.getNormalizedUnit() != null ? m.getNormalizedUnit() : m.getExtractedUnit();
                    return String.format("%s (%s %s - %s)",
                            bName,
                            val != null ? String.format("%.2f", val) : "N/A",
                            u != null ? u : "",
                            m.getStatus());
                })
                .toList();

        boolean isCritical = abnormalMeds.stream().anyMatch(m -> m.getStatus() == MeasurementStatus.CRITICAL);
        String urgency = isCritical ? "URGENT" : "ROUTINE";

        String reason = switch (specialty) {
            case "Endocrinology" -> "Evaluation of abnormal glycemic biomarkers indicating impaired glucose tolerance, insulin resistance, or diabetes progression.";
            case "Nephrology" -> "Assessment of elevated renal clearance or electrolyte markers to investigate potential glomerular filtration issues or kidney stress.";
            case "Cardiology" -> "Comprehensive cardiovascular risk assessment to evaluate lipid profiles, vascular health, or cardiac strain markers.";
            case "Hematology" -> "Investigation of abnormal red cell indices or platelet counts to evaluate potential cytopenias or hematologic etiologies.";
            default -> "Follow-up consultation to review non-standard laboratory values and correlate with clinical examination.";
        };

        String questions = switch (specialty) {
            case "Endocrinology" -> "1. Do my recent glucose/A1c levels suggest prediabetes or diabetes? 2. Should we consider continuous glucose monitoring or nutritional lifestyle intervention?";
            case "Nephrology" -> "1. What is my estimated glomerular filtration rate (eGFR) and what does this creatinine level indicate? 2. Are any of my medications potentially nephrotoxic?";
            case "Cardiology" -> "1. Does my lipid profile require initiation or adjustment of statin therapy? 2. What is my 10-year ASCVD cardiovascular risk score?";
            case "Hematology" -> "1. What is the suspected underlying cause of my abnormal blood cell counts? 2. Are further peripheral blood smear or iron studies indicated?";
            default -> "1. How do these laboratory findings correlate with my medical history? 2. When should these tests be repeated to confirm trends?";
        };

        return new SpecialtyRecommendationDto(specialty, reason, urgency, names, questions);
    }
}
