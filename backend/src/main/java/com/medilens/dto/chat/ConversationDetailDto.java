package com.medilens.dto.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationDetailDto {
    private UUID id;
    private UUID reportId;
    private String reportFilename;
    private String title;
    private List<MessageDto> messages = new ArrayList<>();
    private List<String> suggestedPrompts = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public ConversationDetailDto() {}

    public ConversationDetailDto(UUID id, UUID reportId, String reportFilename, String title,
                                 List<MessageDto> messages, List<String> suggestedPrompts,
                                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.reportId = reportId;
        this.reportFilename = reportFilename;
        this.title = title;
        this.messages = messages;
        this.suggestedPrompts = suggestedPrompts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }

    public void setSuggestedPrompts(List<String> suggestedPrompts) {
        this.suggestedPrompts = suggestedPrompts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
