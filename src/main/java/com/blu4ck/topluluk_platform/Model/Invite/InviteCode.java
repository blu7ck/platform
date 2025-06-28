package com.blu4ck.topluluk_platform.Model.Invite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class InviteCode {

    private String id;

    @JsonProperty("invite_code")
    private String inviteCode;

    @JsonProperty("inviter_id")
    private String inviterId;

    @JsonProperty("inviter_name")
    private String inviterName;

    @JsonProperty("inviter_email")
    private String inviterEmail;

    @JsonProperty("invited_email")
    private String invitedEmail; // Davet edilen kişinin emaili

    @JsonProperty("is_used")
    private Boolean isUsed = false;

    @JsonProperty("used_by_id")
    private String usedById; // Kodu kullanan kişinin ID'si

    @JsonProperty("used_by_email")
    private String usedByEmail; // Kodu kullanan kişinin emaili

    @JsonProperty("used_at")
    private LocalDateTime usedAt;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("invite_type")
    private String inviteType; // "admin", "user", "restricted"

    @JsonProperty("can_invite_others")
    private Boolean canInviteOthers = true; // Admin davetleri true, user davetleri false

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public InviteCode() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public String getInviterId() { return inviterId; }
    public void setInviterId(String inviterId) { this.inviterId = inviterId; }

    public String getInviterName() { return inviterName; }
    public void setInviterName(String inviterName) { this.inviterName = inviterName; }

    public String getInviterEmail() { return inviterEmail; }
    public void setInviterEmail(String inviterEmail) { this.inviterEmail = inviterEmail; }

    public String getInvitedEmail() { return invitedEmail; }
    public void setInvitedEmail(String invitedEmail) { this.invitedEmail = invitedEmail; }

    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

    public String getUsedById() { return usedById; }
    public void setUsedById(String usedById) { this.usedById = usedById; }

    public String getUsedByEmail() { return usedByEmail; }
    public void setUsedByEmail(String usedByEmail) { this.usedByEmail = usedByEmail; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getInviteType() { return inviteType; }
    public void setInviteType(String inviteType) { this.inviteType = inviteType; }

    public Boolean getCanInviteOthers() { return canInviteOthers; }
    public void setCanInviteOthers(Boolean canInviteOthers) { this.canInviteOthers = canInviteOthers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}