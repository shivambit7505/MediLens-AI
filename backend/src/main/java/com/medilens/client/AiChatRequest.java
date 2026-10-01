package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AiChatRequest {
    private String query;
    private String patientContext;
    private List<AiChatBiomarkerDto> recentBiomarkers = new ArrayList<>();
    private List<ChatMessagePayload> history = new ArrayList<>();

    public static class AiChatBiomarkerDto {
        @JsonProperty("canonical_name")
        private String canonicalName;

        @JsonProperty("normalized_value_numeric")
        private Double normalizedValueNumeric;

        @JsonProperty("normalized_unit")
        private String normalizedUnit;

        @JsonProperty("status")
        private String status;

        public AiChatBiomarkerDto() {}

        public AiChatBiomarkerDto(String canonicalName, Double normalizedValueNumeric, String normalizedUnit, String status) {
            this.canonicalName = canonicalName;
            this.normalizedValueNumeric = normalizedValueNumeric;
            this.normalizedUnit = normalizedUnit;
            this.status = status;
        }

        public String getCanonicalName() { return canonicalName; }
        public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }
        public Double getNormalizedValueNumeric() { return normalizedValueNumeric; }
        public void setNormalizedValueNumeric(Double normalizedValueNumeric) { this.normalizedValueNumeric = normalizedValueNumeric; }
        public String getNormalizedUnit() { return normalizedUnit; }
        public void setNormalizedUnit(String normalizedUnit) { this.normalizedUnit = normalizedUnit; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ChatMessagePayload {
        private String role;
        private String content;

        public ChatMessagePayload() {}

        public ChatMessagePayload(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public AiChatRequest() {}

    public AiChatRequest(String query, String patientContext, List<AiChatBiomarkerDto> recentBiomarkers, List<ChatMessagePayload> history) {
        this.query = query;
        this.patientContext = patientContext;
        this.recentBiomarkers = recentBiomarkers;
        this.history = history;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getPatientContext() { return patientContext; }
    public void setPatientContext(String patientContext) { this.patientContext = patientContext; }
    public List<AiChatBiomarkerDto> getRecentBiomarkers() { return recentBiomarkers; }
    public void setRecentBiomarkers(List<AiChatBiomarkerDto> recentBiomarkers) { this.recentBiomarkers = recentBiomarkers; }
    public List<ChatMessagePayload> getHistory() { return history; }
    public void setHistory(List<ChatMessagePayload> history) { this.history = history; }
}
