package com.medilens.dto.medication;

import com.medilens.model.InteractionSeverity;

import java.util.ArrayList;
import java.util.List;

public class InteractionCheckResponse {
    private boolean hasInteractions;
    private int totalInteractionsCount;
    private InteractionSeverity highestSeverity;
    private String highestSeverityBadgeColor;
    private List<DrugInteractionDto> interactions = new ArrayList<>();
    private String clinicalWarning;
    private String statutoryDisclaimer;

    public InteractionCheckResponse() {}

    public InteractionCheckResponse(boolean hasInteractions, int totalInteractionsCount,
                                    InteractionSeverity highestSeverity, String highestSeverityBadgeColor,
                                    List<DrugInteractionDto> interactions, String clinicalWarning,
                                    String statutoryDisclaimer) {
        this.hasInteractions = hasInteractions;
        this.totalInteractionsCount = totalInteractionsCount;
        this.highestSeverity = highestSeverity;
        this.highestSeverityBadgeColor = highestSeverityBadgeColor;
        this.interactions = interactions;
        this.clinicalWarning = clinicalWarning;
        this.statutoryDisclaimer = statutoryDisclaimer;
    }

    public boolean isHasInteractions() {
        return hasInteractions;
    }

    public void setHasInteractions(boolean hasInteractions) {
        this.hasInteractions = hasInteractions;
    }

    public int getTotalInteractionsCount() {
        return totalInteractionsCount;
    }

    public void setTotalInteractionsCount(int totalInteractionsCount) {
        this.totalInteractionsCount = totalInteractionsCount;
    }

    public InteractionSeverity getHighestSeverity() {
        return highestSeverity;
    }

    public void setHighestSeverity(InteractionSeverity highestSeverity) {
        this.highestSeverity = highestSeverity;
    }

    public String getHighestSeverityBadgeColor() {
        return highestSeverityBadgeColor;
    }

    public void setHighestSeverityBadgeColor(String highestSeverityBadgeColor) {
        this.highestSeverityBadgeColor = highestSeverityBadgeColor;
    }

    public List<DrugInteractionDto> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<DrugInteractionDto> interactions) {
        this.interactions = interactions;
    }

    public String getClinicalWarning() {
        return clinicalWarning;
    }

    public void setClinicalWarning(String clinicalWarning) {
        this.clinicalWarning = clinicalWarning;
    }

    public String getStatutoryDisclaimer() {
        return statutoryDisclaimer;
    }

    public void setStatutoryDisclaimer(String statutoryDisclaimer) {
        this.statutoryDisclaimer = statutoryDisclaimer;
    }
}
