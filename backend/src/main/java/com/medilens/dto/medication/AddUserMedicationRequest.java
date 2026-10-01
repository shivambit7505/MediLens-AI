package com.medilens.dto.medication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class AddUserMedicationRequest {

    @NotNull(message = "Medication ID is required")
    private UUID medicationId;

    @NotBlank(message = "Dosage is required (e.g., 500mg, 10mg)")
    private String dosage;

    @NotBlank(message = "Frequency is required (e.g., Once daily, Twice daily with meals)")
    private String frequency;

    private LocalDate startDate;
    private LocalDate endDate;

    public AddUserMedicationRequest() {}

    public AddUserMedicationRequest(UUID medicationId, String dosage, String frequency, LocalDate startDate, LocalDate endDate) {
        this.medicationId = medicationId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(UUID medicationId) {
        this.medicationId = medicationId;
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
}
