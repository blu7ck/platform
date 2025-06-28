package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConversationRequest {

    @JsonProperty("target_user_id")
    private String targetUserId;

    // Constructors
    public ConversationRequest() {}

    // Getters and Setters
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
}