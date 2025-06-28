package com.blu4ck.topluluk_platform.Model;

public class SubscriptionCancelRequest {

    private String email;
    private String reason; // İsteğe bağlı iptal sebebi

    // Constructors
    public SubscriptionCancelRequest() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}