package com.medilens.dto.triage;

public class BiomarkerReadingDto {
    private String canonicalName;
    private Double valueNumeric;
    private String unit;

    public BiomarkerReadingDto() {}

    public BiomarkerReadingDto(String canonicalName, Double valueNumeric, String unit) {
        this.canonicalName = canonicalName;
        this.valueNumeric = valueNumeric;
        this.unit = unit;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
