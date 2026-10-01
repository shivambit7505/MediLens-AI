package com.medilens.dto.medication;

import com.medilens.model.InteractionSeverity;

import java.util.UUID;

public class DrugInteractionDto {
    private UUID id;
    private MedicationDto medicationA;
    private MedicationDto medicationB;
    private InteractionSeverity severity;
    private String severityBadgeColor;
    private String interactionMechanism;
    private String clinicalEvidenceSource;

    public DrugInteractionDto() {}

    public DrugInteractionDto(UUID id, MedicationDto medicationA, MedicationDto medicationB,
                              InteractionSeverity severity, String severityBadgeColor,
                              String interactionMechanism, String clinicalEvidenceSource) {
        this.id = id;
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.severity = severity;
        this.severityBadgeColor = severityBadgeColor;
        this.interactionMechanism = interactionMechanism;
        this.clinicalEvidenceSource = clinicalEvidenceSource;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicationDto getMedicationA() {
        return medicationA;
    }

    public void setMedicationA(MedicationDto medicationA) {
        this.medicationA = medicationA;
    }

    public MedicationDto getMedicationB() {
        return medicationB;
    }

    public void setMedicationB(MedicationDto medicationB) {
        this.medicationB = medicationB;
    }

    public InteractionSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(InteractionSeverity severity) {
        this.severity = severity;
    }

    public String getSeverityBadgeColor() {
        return severityBadgeColor;
    }

    public void setSeverityBadgeColor(String severityBadgeColor) {
        this.severityBadgeColor = severityBadgeColor;
    }

    public String getInteractionMechanism() {
        return interactionMechanism;
    }

    public void setInteractionMechanism(String interactionMechanism) {
        this.interactionMechanism = interactionMechanism;
    }

    public String getClinicalEvidenceSource() {
        return clinicalEvidenceSource;
    }

    public void setClinicalEvidenceSource(String clinicalEvidenceSource) {
        this.clinicalEvidenceSource = clinicalEvidenceSource;
    }
}
