package com.blu4ck.topluluk_platform.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class User {

        private String id;
        private String email;

        @JsonProperty("full_name")
        private String fullName;

        private String title;
        private String website;

        @JsonProperty("success_story")
        private String successStory;

        @JsonProperty("failure_story")
        private String failureStory;

        @JsonProperty("invite_quota")
        private Integer inviteQuota = 5;

        @JsonProperty("used_invites")
        private Integer usedInvites = 0;

        @JsonProperty("collaboration_count")
        private Integer collaborationCount = 0;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        @JsonProperty("is_active")
        private Boolean isActive = true;


        public User() {}


        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }

        public String getSuccessStory() { return successStory; }
        public void setSuccessStory(String successStory) { this.successStory = successStory; }

        public String getFailureStory() { return failureStory; }
        public void setFailureStory(String failureStory) { this.failureStory = failureStory; }

        public Integer getInviteQuota() { return inviteQuota; }
        public void setInviteQuota(Integer inviteQuota) { this.inviteQuota = inviteQuota; }

        public Integer getUsedInvites() { return usedInvites; }
        public void setUsedInvites(Integer usedInvites) { this.usedInvites = usedInvites; }

        public Integer getCollaborationCount() { return collaborationCount; }
        public void setCollaborationCount(Integer collaborationCount) { this.collaborationCount = collaborationCount; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
