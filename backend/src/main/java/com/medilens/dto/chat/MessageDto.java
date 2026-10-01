package com.medilens.dto.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MessageDto {
    private UUID id;
    private String sender; // "USER" or "ASSISTANT"
    private String content;
    private List<String> citedSources;
    private Instant createdAt;

    public MessageDto() {}

    public MessageDto(UUID id, String sender, String content, List<String> citedSources, Instant createdAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.citedSources = citedSources;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getCitedSources() {
        return citedSources;
    }

    public void setCitedSources(List<String> citedSources) {
        this.citedSources = citedSources;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
