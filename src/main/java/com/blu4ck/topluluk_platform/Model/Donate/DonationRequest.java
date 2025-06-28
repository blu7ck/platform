package com.blu4ck.topluluk_platform.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class DonationRequest {

    private BigDecimal amount;
    private String currency; // "TRY" veya "USD"

    @JsonProperty("donation_type")
    private String donationType; // "general" veya "event"

    @JsonProperty("event_id")
    private String eventId; // Eğer event bağışı ise

    private String message;

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous = false;

    @JsonProperty("donor_name")
    private String donorName;

    @JsonProperty("donor_email")
    private String donorEmail;

    // Constructors
    public DonationRequest() {}

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDonationType() { return donationType; }
    public void setDonationType(String donationType) { this.donationType = donationType; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorEmail() { return donorEmail; }
    public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }
}