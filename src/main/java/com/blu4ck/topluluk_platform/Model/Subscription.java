package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Subscription {

    private String id;

    @JsonProperty("subscriber_name")
    private String subscriberName; // Anonim ise null

    @JsonProperty("subscriber_email")
    private String subscriberEmail; // Anonim ise null

    private BigDecimal amount;
    private String currency; // "TRY", "USD"

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous = false;

    private String status; // "active", "cancelled", "paused"

    private String message;

    @JsonProperty("payment_method_id")
    private String paymentMethodId; // Iyzico subscription ID

    @JsonProperty("next_payment_date")
    private LocalDateTime nextPaymentDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;

    @JsonProperty("is_active")
    private Boolean isActive = true;

    // Constructors
    public Subscription() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public String getSubscriberEmail() { return subscriberEmail; }
    public void setSubscriberEmail(String subscriberEmail) { this.subscriberEmail = subscriberEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public LocalDateTime getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDateTime nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}