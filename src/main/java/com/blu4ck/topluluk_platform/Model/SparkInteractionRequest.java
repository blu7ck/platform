package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SparkInteractionRequest {

    @JsonProperty("interaction_type")
    private String interactionType;

    // Constructors
    public SparkInteractionRequest() {}

    // Getters and Setters
    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
}