package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlowCreateRequest {

    private String title;
    private String content;

    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration = false;

    @JsonProperty("allow_sharing")
    private Boolean allowSharing = false;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getAllowCollaboration() { return allowCollaboration; }
    public void setAllowCollaboration(Boolean allowCollaboration) { this.allowCollaboration = allowCollaboration; }

    public Boolean getAllowSharing() { return allowSharing; }
    public void setAllowSharing(Boolean allowSharing) { this.allowSharing = allowSharing; }
}