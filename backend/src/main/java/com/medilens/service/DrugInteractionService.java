package com.medilens.service;

import com.medilens.dto.medication.*;
import com.medilens.exception.ResourceNotFoundException;
import com.medilens.model.*;
import com.medilens.repository.DrugInteractionRepository;
import com.medilens.repository.MedicationRepository;
import com.medilens.repository.UserMedicationRepository;
import com.medilens.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DrugInteractionService {

    private final MedicationRepository medicationRepository;
    private final UserMedicationRepository userMedicationRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final UserRepository userRepository;

    public DrugInteractionService(MedicationRepository medicationRepository,
                                  UserMedicationRepository userMedicationRepository,
                                  DrugInteractionRepository drugInteractionRepository,
                                  UserRepository userRepository) {
        this.medicationRepository = medicationRepository;
        this.userMedicationRepository = userMedicationRepository;
        this.drugInteractionRepository = drugInteractionRepository;
        this.userRepository = userRepository;
    }

    public List<MedicationDto> searchMedications(String query) {
        List<Medication> meds;
        if (query == null || query.isBlank()) {
            meds = medicationRepository.findAll();
        } else {
            meds = medicationRepository.searchByName(query.trim());
        }
        return meds.stream().map(this::mapMedicationToDto).toList();
    }

    public List<UserMedicationDto> getUserActiveMedications(UUID userId) {
        return userMedicationRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(this::mapUserMedicationToDto)
                .toList();
    }

    @Transactional
    public UserMedicationDto addUserMedication(UUID userId, AddUserMedicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication", "id", request.getMedicationId()));

        UserMedication userMed = new UserMedication();
        userMed.setUser(user);
        userMed.setMedication(medication);
        userMed.setDosage(request.getDosage());
        userMed.setFrequency(request.getFrequency());
        userMed.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        userMed.setEndDate(request.getEndDate());
        userMed.setActive(true);

        UserMedication saved = userMedicationRepository.save(userMed);
        return mapUserMedicationToDto(saved);
    }

    @Transactional
    public void removeUserMedication(UUID userId, UUID userMedicationId) {
        UserMedication userMed = userMedicationRepository.findById(userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("UserMedication", "id", userMedicationId));

        if (!userMed.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify medication for another user");
        }

        userMed.setActive(false);
        userMedicationRepository.save(userMed);
    }

    public InteractionCheckResponse checkInteractionsForUser(UUID userId) {
        List<UserMedication> userMeds = userMedicationRepository.findByUserIdAndIsActiveTrue(userId);
        List<UUID> medIds = userMeds.stream().map(um -> um.getMedication().getId()).toList();
        return checkInteractionsForMedicationIds(medIds);
    }

    public InteractionCheckResponse checkInteractionsForMedicationIds(List<UUID> medicationIds) {
        if (medicationIds == null || medicationIds.size() < 2) {
            return new InteractionCheckResponse(
                    false,
                    0,
                    null,
                    "emerald",
                    Collections.emptyList(),
                    "At least two medications are required to perform a pairwise drug-drug interaction evaluation.",
                    getStatutoryDisclaimer()
            );
        }

        // Deduplicate
        List<UUID> distinctIds = new ArrayList<>(new LinkedHashSet<>(medicationIds));
        List<DrugInteractionDto> detectedInteractions = new ArrayList<>();

        for (int i = 0; i < distinctIds.size(); i++) {
            for (int j = i + 1; j < distinctIds.size(); j++) {
                UUID idA = distinctIds.get(i);
                UUID idB = distinctIds.get(j);

                Optional<DrugInteraction> match = drugInteractionRepository.findInteractionBetween(idA, idB);
                if (match.isPresent()) {
                    DrugInteraction di = match.get();
                    detectedInteractions.add(new DrugInteractionDto(
                            di.getId(),
                            mapMedicationToDto(di.getMedicationA()),
                            mapMedicationToDto(di.getMedicationB()),
                            di.getSeverity(),
                            mapSeverityToColor(di.getSeverity()),
                            di.getInteractionMechanism(),
                            di.getClinicalEvidenceSource()
                    ));
                }
            }
        }

        boolean hasInteractions = !detectedInteractions.isEmpty();
        InteractionSeverity highestSeverity = null;
        String highestBadgeColor = "emerald";
        String clinicalWarning;

        if (hasInteractions) {
            highestSeverity = determineHighestSeverity(detectedInteractions);
            highestBadgeColor = mapSeverityToColor(highestSeverity);

            clinicalWarning = switch (highestSeverity) {
                case CONTRAINDICATED -> "CRITICAL SAFETY WARNING: One or more medication combinations are strictly contraindicated due to severe toxicity, major hemorrhaging, or fatal arrhythmias. Consult your prescribing physician or pharmacist immediately.";
                case MAJOR -> "HIGH ALERT: Clinically significant drug interactions identified that may amplify adverse effects or nullify therapeutic efficacy. Clinical dose adjustment or alternative therapy is strongly recommended.";
                case MODERATE -> "MODERATE INTERACTION: Concomitant use may warrant periodic clinical monitoring (e.g. serum potassium, renal profile, or blood pressure tracking).";
                case MINOR -> "MINOR INTERACTION: Mild or negligible pharmacokinetic alteration. Usually manageable without alteration to medication regimens.";
            };
        } else {
            clinicalWarning = "No known contraindicated or adverse pairwise interactions detected between the selected medications in the current clinical matrix.";
        }

        return new InteractionCheckResponse(
                hasInteractions,
                detectedInteractions.size(),
                highestSeverity,
                highestBadgeColor,
                detectedInteractions,
                clinicalWarning,
                getStatutoryDisclaimer()
        );
    }

    private InteractionSeverity determineHighestSeverity(List<DrugInteractionDto> interactions) {
        if (interactions.stream().anyMatch(i -> i.getSeverity() == InteractionSeverity.CONTRAINDICATED)) {
            return InteractionSeverity.CONTRAINDICATED;
        }
        if (interactions.stream().anyMatch(i -> i.getSeverity() == InteractionSeverity.MAJOR)) {
            return InteractionSeverity.MAJOR;
        }
        if (interactions.stream().anyMatch(i -> i.getSeverity() == InteractionSeverity.MODERATE)) {
            return InteractionSeverity.MODERATE;
        }
        return InteractionSeverity.MINOR;
    }

    private String mapSeverityToColor(InteractionSeverity severity) {
        if (severity == null) return "emerald";
        return switch (severity) {
            case CONTRAINDICATED -> "red";
            case MAJOR -> "rose";
            case MODERATE -> "amber";
            case MINOR -> "blue";
        };
    }

    private MedicationDto mapMedicationToDto(Medication m) {
        return new MedicationDto(
                m.getId(),
                m.getBrandName(),
                m.getGenericName(),
                m.getRxnormCui(),
                m.getTherapeuticClass(),
                m.getStandardDosageGuidelines()
        );
    }

    private UserMedicationDto mapUserMedicationToDto(UserMedication um) {
        return new UserMedicationDto(
                um.getId(),
                mapMedicationToDto(um.getMedication()),
                um.getDosage(),
                um.getFrequency(),
                um.getStartDate(),
                um.getEndDate(),
                um.isActive()
        );
    }

    private String getStatutoryDisclaimer() {
        return "DISCLAIMER: MediLens AI drug interaction warnings are based on published clinical pharmacology reference matrices (FDA, Lexicomp, Mayo Clinic). This tool is for patient education and clinical navigation only. Never alter, reduce, or discontinue prescribed pharmacotherapy without consulting your prescribing healthcare provider.";
    }
}
