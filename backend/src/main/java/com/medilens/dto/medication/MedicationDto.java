package com.medilens.dto.medication;

import java.util.UUID;

public class MedicationDto {
    private UUID id;
    private String brandName;
    private String genericName;
    private String rxnormCui;
    private String therapeuticClass;
    private String standardDosageGuidelines;

    public MedicationDto() {}

    public MedicationDto(UUID id, String brandName, String genericName, String rxnormCui,
                         String therapeuticClass, String standardDosageGuidelines) {
        this.id = id;
        this.brandName = brandName;
        this.genericName = genericName;
        this.rxnormCui = rxnormCui;
        this.therapeuticClass = therapeuticClass;
        this.standardDosageGuidelines = standardDosageGuidelines;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getRxnormCui() {
        return rxnormCui;
    }

    public void setRxnormCui(String rxnormCui) {
        this.rxnormCui = rxnormCui;
    }

    public String getTherapeuticClass() {
        return therapeuticClass;
    }

    public void setTherapeuticClass(String therapeuticClass) {
        this.therapeuticClass = therapeuticClass;
    }

    public String getStandardDosageGuidelines() {
        return standardDosageGuidelines;
    }

    public void setStandardDosageGuidelines(String standardDosageGuidelines) {
        this.standardDosageGuidelines = standardDosageGuidelines;
    }
}
