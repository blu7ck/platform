package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SparkCreateRequest {

    private String content;

    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration = false;

    // Constructors
    public SparkCreateRequest() {}

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getAllowCollaboration() { return allowCollaboration; }
    public void setAllowCollaboration(Boolean allowCollaboration) { this.allowCollaboration = allowCollaboration; }
}