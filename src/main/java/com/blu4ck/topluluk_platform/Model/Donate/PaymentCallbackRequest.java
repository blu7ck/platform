package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentCallbackRequest {

    private String type; // "donation" veya "subscription"

    @JsonProperty("resource_id")
    private String resourceId; // donation_id veya subscription_id

    private String status; // "completed", "failed", "cancelled"

    @JsonProperty("payment_id")
    private String paymentId; // Iyzico payment ID

    // Constructors
    public PaymentCallbackRequest() {}

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}