package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Donation {

    private String id;

    @JsonProperty("donor_name")
    private String donorName; // Anonim ise null

    @JsonProperty("donor_email")
    private String donorEmail; // Anonim ise null

    private BigDecimal amount;
    private String currency; // "TRY", "USD"

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous = false;

    @JsonProperty("donation_type")
    private String donationType; // "general", "event"

    @JsonProperty("event_id")
    private String eventId; // Eğer özel etkinlik bağışı ise

    private String message;

    @JsonProperty("payment_status")
    private String paymentStatus; // "pending", "completed", "failed"

    @JsonProperty("payment_id")
    private String paymentId; // Iyzico payment ID

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public Donation() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorEmail() { return donorEmail; }
    public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public String getDonationType() { return donationType; }
    public void setDonationType(String donationType) { this.donationType = donationType; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}