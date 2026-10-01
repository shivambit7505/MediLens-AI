package com.medilens.client;

import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {
    private String reply;
    private List<AiRagResponse.AiEvidenceSource> citedSources = new ArrayList<>();
    private List<String> suggestedFollowups = new ArrayList<>();
    private String disclaimer;
    private boolean guardrailPassed = true;

    public AiChatResponse() {}

    public AiChatResponse(String reply, List<AiRagResponse.AiEvidenceSource> citedSources,
                          List<String> suggestedFollowups, String disclaimer, boolean guardrailPassed) {
        this.reply = reply;
        this.citedSources = citedSources;
        this.suggestedFollowups = suggestedFollowups;
        this.disclaimer = disclaimer;
        this.guardrailPassed = guardrailPassed;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public List<AiRagResponse.AiEvidenceSource> getCitedSources() { return citedSources; }
    public void setCitedSources(List<AiRagResponse.AiEvidenceSource> citedSources) { this.citedSources = citedSources; }
    public List<String> getSuggestedFollowups() { return suggestedFollowups; }
    public void setSuggestedFollowups(List<String> suggestedFollowups) { this.suggestedFollowups = suggestedFollowups; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
    public boolean isGuardrailPassed() { return guardrailPassed; }
    public void setGuardrailPassed(boolean guardrailPassed) { this.guardrailPassed = guardrailPassed; }
}
