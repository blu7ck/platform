package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Spark {

//    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("author_name")
    private String authorName;

    private String content;

    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration = false;

    @JsonProperty("interaction_count")
    private Integer interactionCount = 0;

//    @JsonProperty("created_at")
//    private LocalDateTime createdAt;
//
//    @JsonProperty("updated_at")
//    private LocalDateTime updatedAt;
//
//    @JsonProperty("is_active")
//    private Boolean isActive = true;

    // Constructors
    public Spark() {}

    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getAllowCollaboration() { return allowCollaboration; }
    public void setAllowCollaboration(Boolean allowCollaboration) { this.allowCollaboration = allowCollaboration; }

    public Integer getInteractionCount() { return interactionCount; }
    public void setInteractionCount(Integer interactionCount) { this.interactionCount = interactionCount; }

//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//    public Boolean getIsActive() { return isActive; }
//    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}