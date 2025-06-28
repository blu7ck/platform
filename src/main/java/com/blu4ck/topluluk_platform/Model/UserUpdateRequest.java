package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserUpdateRequest {

    @JsonProperty("full_name")
    private String fullName;

    private String title;
    private String website;

    @JsonProperty("success_story")
    private String successStory;

    @JsonProperty("failure_story")
    private String failureStory;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getSuccessStory() { return successStory; }
    public void setSuccessStory(String successStory) { this.successStory = successStory; }

    public String getFailureStory() { return failureStory; }
    public void setFailureStory(String failureStory) {
        this.failureStory = failureStory;
    }
}
