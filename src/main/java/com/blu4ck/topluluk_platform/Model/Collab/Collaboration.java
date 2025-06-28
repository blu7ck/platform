package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Collaboration {

    private String id;

    @JsonProperty("initiator_id")
    private String initiatorId;

    @JsonProperty("participant_id")
    private String participantId;

    @JsonProperty("initiator_name")
    private String initiatorName;

    @JsonProperty("participant_name")
    private String participantName;

    @JsonProperty("source_type")
    private String sourceType; // "flow", "spark", "profile"

    @JsonProperty("source_id")
    private String sourceId;

    private String status; // "pending", "approved", "active", "completed", "cancelled"

    private String title;
    private String description;

    @JsonProperty("is_premium")
    private Boolean isPremium = false;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public Collaboration() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInitiatorId() { return initiatorId; }
    public void setInitiatorId(String initiatorId) { this.initiatorId = initiatorId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getInitiatorName() { return initiatorName; }
    public void setInitiatorName(String initiatorName) { this.initiatorName = initiatorName; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}