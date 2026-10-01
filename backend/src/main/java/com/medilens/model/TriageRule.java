package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "triage_rules")
public class TriageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_name", nullable = false, unique = true, length = 255)
    private String ruleName;

    @Column(name = "biomarker_canonical_name", nullable = false, length = 200)
    private String biomarkerCanonicalName;

    @Column(name = "comparison_operator", nullable = false, length = 10)
    private String comparisonOperator; // ">", ">=", "<", "<=", "=="

    @Column(name = "threshold_numeric", nullable = false)
    private Double thresholdNumeric;

    @Column(length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false, length = 50)
    private TriageUrgency urgencyLevel;

    @Column(name = "deterministic_action_instruction", nullable = false, columnDefinition = "TEXT")
    private String deterministicActionInstruction;

    @Column(name = "disclaimer_text", nullable = false, columnDefinition = "TEXT")
    private String disclaimerText;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TriageRule() {}

    public TriageRule(UUID id, String ruleName, String biomarkerCanonicalName, String comparisonOperator,
                      Double thresholdNumeric, String unit, TriageUrgency urgencyLevel,
                      String deterministicActionInstruction, String disclaimerText,
                      boolean isActive, Instant createdAt) {
        this.id = id;
        this.ruleName = ruleName;
        this.biomarkerCanonicalName = biomarkerCanonicalName;
        this.comparisonOperator = comparisonOperator;
        this.thresholdNumeric = thresholdNumeric;
        this.unit = unit;
        this.urgencyLevel = urgencyLevel;
        this.deterministicActionInstruction = deterministicActionInstruction;
        this.disclaimerText = disclaimerText;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getBiomarkerCanonicalName() {
        return biomarkerCanonicalName;
    }

    public void setBiomarkerCanonicalName(String biomarkerCanonicalName) {
        this.biomarkerCanonicalName = biomarkerCanonicalName;
    }

    public String getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public Double getThresholdNumeric() {
        return thresholdNumeric;
    }

    public void setThresholdNumeric(Double thresholdNumeric) {
        this.thresholdNumeric = thresholdNumeric;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public TriageUrgency getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(TriageUrgency urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getDeterministicActionInstruction() {
        return deterministicActionInstruction;
    }

    public void setDeterministicActionInstruction(String deterministicActionInstruction) {
        this.deterministicActionInstruction = deterministicActionInstruction;
    }

    public String getDisclaimerText() {
        return disclaimerText;
    }

    public void setDisclaimerText(String disclaimerText) {
        this.disclaimerText = disclaimerText;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
