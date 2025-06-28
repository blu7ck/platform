package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class SparkInteraction {


    @JsonProperty("spark_id")
    private String sparkId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("interaction_type")
    private String interactionType; // "Yap!", "Bekle", "Para odaklı düşün", vs.

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public SparkInteraction() {}

    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }

    public String getSparkId() { return sparkId; }
    public void setSparkId(String sparkId) { this.sparkId = sparkId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}