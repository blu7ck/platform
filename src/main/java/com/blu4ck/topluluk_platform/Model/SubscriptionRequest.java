package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class SubscriptionRequest {

    private BigDecimal amount;
    private String currency; // "TRY" veya "USD"
    private String message;

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous = false;

    @JsonProperty("subscriber_name")
    private String subscriberName;

    @JsonProperty("subscriber_email")
    private String subscriberEmail;

    // Constructors
    public SubscriptionRequest() {}

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public String getSubscriberEmail() { return subscriberEmail; }
    public void setSubscriberEmail(String subscriberEmail) { this.subscriberEmail = subscriberEmail; }
}