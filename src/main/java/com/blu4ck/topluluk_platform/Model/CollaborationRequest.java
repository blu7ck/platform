package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollaborationRequest {

    @JsonProperty("participant_id")
    private String participantId;

    @JsonProperty("source_type")
    private String sourceType; // "flow", "spark", "profile"

    @JsonProperty("source_id")
    private String sourceId;

    private String title;
    private String description;

    // Constructors
    public CollaborationRequest() {}

    // Getters and Setters
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}