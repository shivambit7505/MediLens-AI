package com.medilens.dto.triage;

import com.medilens.model.TriageUrgency;

import java.util.ArrayList;
import java.util.List;

public class TriageEvaluationResponse {
    private TriageUrgency overallUrgency;
    private boolean emergencyFlag;
    private String urgencyBadgeColor;
    private String primaryActionDirective;
    private List<TriageTriggerDto> triggers = new ArrayList<>();
    private List<String> recommendedSpecialties = new ArrayList<>();
    private String disclaimerText;

    public TriageEvaluationResponse() {}

    public TriageEvaluationResponse(TriageUrgency overallUrgency, boolean emergencyFlag,
                                    String urgencyBadgeColor, String primaryActionDirective,
                                    List<TriageTriggerDto> triggers, List<String> recommendedSpecialties,
                                    String disclaimerText) {
        this.overallUrgency = overallUrgency;
        this.emergencyFlag = emergencyFlag;
        this.urgencyBadgeColor = urgencyBadgeColor;
        this.primaryActionDirective = primaryActionDirective;
        this.triggers = triggers;
        this.recommendedSpecialties = recommendedSpecialties;
        this.disclaimerText = disclaimerText;
    }

    public TriageUrgency getOverallUrgency() {
        return overallUrgency;
    }

    public void setOverallUrgency(TriageUrgency overallUrgency) {
        this.overallUrgency = overallUrgency;
    }

    public boolean isEmergencyFlag() {
        return emergencyFlag;
    }

    public void setEmergencyFlag(boolean emergencyFlag) {
        this.emergencyFlag = emergencyFlag;
    }

    public String getUrgencyBadgeColor() {
        return urgencyBadgeColor;
    }

    public void setUrgencyBadgeColor(String urgencyBadgeColor) {
        this.urgencyBadgeColor = urgencyBadgeColor;
    }

    public String getPrimaryActionDirective() {
        return primaryActionDirective;
    }

    public void setPrimaryActionDirective(String primaryActionDirective) {
        this.primaryActionDirective = primaryActionDirective;
    }

    public List<TriageTriggerDto> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriageTriggerDto> triggers) {
        this.triggers = triggers;
    }

    public List<String> getRecommendedSpecialties() {
        return recommendedSpecialties;
    }

    public void setRecommendedSpecialties(List<String> recommendedSpecialties) {
        this.recommendedSpecialties = recommendedSpecialties;
    }

    public String getDisclaimerText() {
        return disclaimerText;
    }

    public void setDisclaimerText(String disclaimerText) {
        this.disclaimerText = disclaimerText;
    }
}
