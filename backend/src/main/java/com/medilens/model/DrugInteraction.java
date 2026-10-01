package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drug_interactions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_drug_pair", columnNames = {"medication_a_id", "medication_b_id"})
})
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InteractionSeverity severity;

    @Column(name = "interaction_mechanism", nullable = false, columnDefinition = "TEXT")
    private String interactionMechanism;

    @Column(name = "clinical_evidence_source", nullable = false, length = 500)
    private String clinicalEvidenceSource;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DrugInteraction() {}

    public DrugInteraction(UUID id, Medication medicationA, Medication medicationB,
                           InteractionSeverity severity, String interactionMechanism,
                           String clinicalEvidenceSource, Instant createdAt) {
        this.id = id;
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.severity = severity;
        this.interactionMechanism = interactionMechanism;
        this.clinicalEvidenceSource = clinicalEvidenceSource;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Medication getMedicationA() {
        return medicationA;
    }

    public void setMedicationA(Medication medicationA) {
        this.medicationA = medicationA;
    }

    public Medication getMedicationB() {
        return medicationB;
    }

    public void setMedicationB(Medication medicationB) {
        this.medicationB = medicationB;
    }

    public InteractionSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(InteractionSeverity severity) {
        this.severity = severity;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
