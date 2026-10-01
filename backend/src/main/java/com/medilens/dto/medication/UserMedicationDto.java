package com.medilens.dto.medication;

import java.time.LocalDate;
import java.util.UUID;

public class UserMedicationDto {
    private UUID id;
    private MedicationDto medication;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public UserMedicationDto() {}

    public UserMedicationDto(UUID id, MedicationDto medication, String dosage,
                             String frequency, LocalDate startDate, LocalDate endDate, boolean active) {
        this.id = id;
        this.medication = medication;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicationDto getMedication() {
        return medication;
    }

    public void setMedication(MedicationDto medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
