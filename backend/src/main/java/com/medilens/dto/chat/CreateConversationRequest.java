package com.medilens.dto.chat;

import java.util.UUID;

public class CreateConversationRequest {
    private UUID reportId;
    private String title;
    private String initialMessage;

    public CreateConversationRequest() {}

    public CreateConversationRequest(UUID reportId, String title, String initialMessage) {
        this.reportId = reportId;
        this.title = title;
        this.initialMessage = initialMessage;
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

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }
}
