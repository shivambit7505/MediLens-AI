package com.medilens.dto.provider;

import java.util.ArrayList;
import java.util.List;

public class SpecialtyRecommendationDto {
    private String specialty;
    private String clinicalReason;
    private String urgency; // "URGENT", "ROUTINE"
    private List<String> triggeringBiomarkers = new ArrayList<>();
    private String suggestedQuestionsForDoctor;

    public SpecialtyRecommendationDto() {}

    public SpecialtyRecommendationDto(String specialty, String clinicalReason, String urgency,
                                      List<String> triggeringBiomarkers, String suggestedQuestionsForDoctor) {
        this.specialty = specialty;
        this.clinicalReason = clinicalReason;
        this.urgency = urgency;
        this.triggeringBiomarkers = triggeringBiomarkers;
        this.suggestedQuestionsForDoctor = suggestedQuestionsForDoctor;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getClinicalReason() {
        return clinicalReason;
    }

    public void setClinicalReason(String clinicalReason) {
        this.clinicalReason = clinicalReason;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public List<String> getTriggeringBiomarkers() {
        return triggeringBiomarkers;
    }

    public void setTriggeringBiomarkers(List<String> triggeringBiomarkers) {
        this.triggeringBiomarkers = triggeringBiomarkers;
    }

    public String getSuggestedQuestionsForDoctor() {
        return suggestedQuestionsForDoctor;
    }

    public void setSuggestedQuestionsForDoctor(String suggestedQuestionsForDoctor) {
        this.suggestedQuestionsForDoctor = suggestedQuestionsForDoctor;
    }
}
