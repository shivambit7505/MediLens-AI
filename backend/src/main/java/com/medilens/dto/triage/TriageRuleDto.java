package com.medilens.dto.triage;

import com.medilens.model.TriageUrgency;

import java.util.UUID;

public class TriageRuleDto {
    private UUID id;
    private String ruleName;
    private String biomarkerCanonicalName;
    private String comparisonOperator;
    private Double thresholdNumeric;
    private String unit;
    private TriageUrgency urgencyLevel;
    private String deterministicActionInstruction;
    private String disclaimerText;

    public TriageRuleDto() {}

    public TriageRuleDto(UUID id, String ruleName, String biomarkerCanonicalName, String comparisonOperator,
                         Double thresholdNumeric, String unit, TriageUrgency urgencyLevel,
                         String deterministicActionInstruction, String disclaimerText) {
        this.id = id;
        this.ruleName = ruleName;
        this.biomarkerCanonicalName = biomarkerCanonicalName;
        this.comparisonOperator = comparisonOperator;
        this.thresholdNumeric = thresholdNumeric;
        this.unit = unit;
        this.urgencyLevel = urgencyLevel;
        this.deterministicActionInstruction = deterministicActionInstruction;
        this.disclaimerText = disclaimerText;
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
}
