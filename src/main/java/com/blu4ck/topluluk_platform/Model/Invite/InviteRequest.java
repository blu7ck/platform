package com.blu4ck.topluluk_platform.Model.Invite;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class InviteRequest {

    @JsonProperty("email_addresses")
    private List<@Email(message = "Geçersiz e-posta adresi") @NotBlank String> emailAddresses;

    @Size(max = 500, message = "Mesaj en fazla 500 karakter olabilir")
    private String message;

    @JsonProperty("invite_type")
    private String inviteType = "user"; // "admin", "user"

    // Constructors
    public InviteRequest() {}

    // Getters and Setters
    public List<String> getEmailAddresses() { return emailAddresses; }
    public void setEmailAddresses(List<String> emailAddresses) { this.emailAddresses = emailAddresses; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getInviteType() { return inviteType; }
    public void setInviteType(String inviteType) { this.inviteType = inviteType; }
}