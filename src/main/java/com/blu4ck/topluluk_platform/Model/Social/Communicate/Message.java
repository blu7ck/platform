package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Message {

    private String id;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("sender_name")
    private String senderName;

    private String content;

    @JsonProperty("is_read")
    private Boolean isRead = false;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public Message() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}