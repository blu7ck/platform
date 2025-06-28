package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SparkSettingsRequest {

    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration;

    // Constructors
    public SparkSettingsRequest() {}

    // Getters and Setters
    public Boolean getAllowCollaboration() { return allowCollaboration; }
    public void setAllowCollaboration(Boolean allowCollaboration) { this.allowCollaboration = allowCollaboration; }
}