package com.medilens.dto.medication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractionCheckRequest {
    private List<UUID> medicationIds = new ArrayList<>();

    public InteractionCheckRequest() {}

    public InteractionCheckRequest(List<UUID> medicationIds) {
        this.medicationIds = medicationIds;
    }

    public List<UUID> getMedicationIds() {
        return medicationIds;
    }

    public void setMedicationIds(List<UUID> medicationIds) {
        this.medicationIds = medicationIds;
    }
}
