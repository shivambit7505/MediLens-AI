package com.medilens.dto.triage;

import java.util.ArrayList;
import java.util.List;

public class TriageEvaluationRequest {
    private List<BiomarkerReadingDto> readings = new ArrayList<>();
    private List<String> symptoms = new ArrayList<>();
    private boolean includeLatestReportBiomarkers = true;

    public TriageEvaluationRequest() {}

    public TriageEvaluationRequest(List<BiomarkerReadingDto> readings, List<String> symptoms, boolean includeLatestReportBiomarkers) {
        this.readings = readings;
        this.symptoms = symptoms;
        this.includeLatestReportBiomarkers = includeLatestReportBiomarkers;
    }

    public List<BiomarkerReadingDto> getReadings() {
        return readings;
    }

    public void setReadings(List<BiomarkerReadingDto> readings) {
        this.readings = readings;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public boolean isIncludeLatestReportBiomarkers() {
        return includeLatestReportBiomarkers;
    }

    public void setIncludeLatestReportBiomarkers(boolean includeLatestReportBiomarkers) {
        this.includeLatestReportBiomarkers = includeLatestReportBiomarkers;
    }
}
