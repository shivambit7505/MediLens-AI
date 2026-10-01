package com.medilens.dto.chat;

import java.time.Instant;
import java.util.UUID;

public class ConversationDto {
    private UUID id;
    private UUID reportId;
    private String title;
    private int messageCount;
    private String lastMessageSnippet;
    private Instant updatedAt;

    public ConversationDto() {}

    public ConversationDto(UUID id, UUID reportId, String title, int messageCount, String lastMessageSnippet, Instant updatedAt) {
        this.id = id;
        this.reportId = reportId;
        this.title = title;
        this.messageCount = messageCount;
        this.lastMessageSnippet = lastMessageSnippet;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getLastMessageSnippet() {
        return lastMessageSnippet;
    }

    public void setLastMessageSnippet(String lastMessageSnippet) {
        this.lastMessageSnippet = lastMessageSnippet;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
