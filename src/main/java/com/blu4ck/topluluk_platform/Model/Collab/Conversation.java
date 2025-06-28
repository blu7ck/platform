package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Conversation {

    private String id;

    @JsonProperty("user1_id")
    private String user1Id;

    @JsonProperty("user2_id")
    private String user2Id;

    @JsonProperty("user1_name")
    private String user1Name;

    @JsonProperty("user2_name")
    private String user2Name;

    @JsonProperty("conversation_type")
    private String conversationType; // "general", "collaboration"

    @JsonProperty("collaboration_id")
    private String collaborationId; // Eğer işbirliği mesajlaşması ise

    @JsonProperty("last_message")
    private String lastMessage;

    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public Conversation() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser1Id() { return user1Id; }
    public void setUser1Id(String user1Id) { this.user1Id = user1Id; }

    public String getUser2Id() { return user2Id; }
    public void setUser2Id(String user2Id) { this.user2Id = user2Id; }

    public String getUser1Name() { return user1Name; }
    public void setUser1Name(String user1Name) { this.user1Name = user1Name; }

    public String getUser2Name() { return user2Name; }
    public void setUser2Name(String user2Name) { this.user2Name = user2Name; }

    public String getConversationType() { return conversationType; }
    public void setConversationType(String conversationType) { this.conversationType = conversationType; }

    public String getCollaborationId() { return collaborationId; }
    public void setCollaborationId(String collaborationId) { this.collaborationId = collaborationId; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}