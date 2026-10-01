package com.medilens.dto.triage;

import com.medilens.model.TriageUrgency;

public class TriageTriggerDto {
    private String triggerType; // "BIOMARKER" or "SYMPTOM"
    private String name;
    private Double observedValue;
    private String operator;
    private Double threshold;
    private String unit;
    private TriageUrgency urgencyLevel;
    private String clinicalInstruction;
    private String rationale;

    public TriageTriggerDto() {}

    public TriageTriggerDto(String triggerType, String name, Double observedValue,
                            String operator, Double threshold, String unit,
                            TriageUrgency urgencyLevel, String clinicalInstruction, String rationale) {
        this.triggerType = triggerType;
        this.name = name;
        this.observedValue = observedValue;
        this.operator = operator;
        this.threshold = threshold;
        this.unit = unit;
        this.urgencyLevel = urgencyLevel;
        this.clinicalInstruction = clinicalInstruction;
        this.rationale = rationale;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getObservedValue() {
        return observedValue;
    }

    public void setObservedValue(Double observedValue) {
        this.observedValue = observedValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
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

    public String getClinicalInstruction() {
        return clinicalInstruction;
    }

    public void setClinicalInstruction(String clinicalInstruction) {
        this.clinicalInstruction = clinicalInstruction;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
}
