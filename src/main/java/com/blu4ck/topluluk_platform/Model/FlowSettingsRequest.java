package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlowSettingsRequest {

    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration;

    @JsonProperty("allow_sharing")
    private Boolean allowSharing;

    // Constructors
    public FlowSettingsRequest() {}

    // Getters and Setters
    public Boolean getAllowCollaboration() { return allowCollaboration; }
    public void setAllowCollaboration(Boolean allowCollaboration) { this.allowCollaboration = allowCollaboration; }

    public Boolean getAllowSharing() { return allowSharing; }
    public void setAllowSharing(Boolean allowSharing) { this.allowSharing = allowSharing; }
}