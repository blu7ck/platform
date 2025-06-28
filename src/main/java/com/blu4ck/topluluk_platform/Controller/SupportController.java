package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.*;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private SupabaseClient supabaseClient;

    // Önceden tanımlı abonelik miktarları
    private final List<BigDecimal> SUBSCRIPTION_AMOUNTS_TRY = Arrays.asList(
            new BigDecimal("25"), new BigDecimal("50"), new BigDecimal("100"), new BigDecimal("250")
    );

    private final List<BigDecimal> SUBSCRIPTION_AMOUNTS_USD = Arrays.asList(
            new BigDecimal("5"), new BigDecimal("10"), new BigDecimal("25"), new BigDecimal("50")
    );

    @GetMapping("/donation-options")
    public ResponseEntity<?> getDonationOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("currencies", Arrays.asList("TRY", "USD"));
        options.put("subscription_amounts_try", SUBSCRIPTION_AMOUNTS_TRY);
        options.put("subscription_amounts_usd", SUBSCRIPTION_AMOUNTS_USD);
        options.put("donation_types", Arrays.asList("general", "event"));

        return ResponseEntity.ok(options);
    }

    @PostMapping("/donate")
    public ResponseEntity<?> createDonation(@RequestBody DonationRequest donationRequest) {
        try {
            // Bağış objesini oluştur
            Donation newDonation = new Donation();
            newDonation.setAmount(donationRequest.getAmount());
            newDonation.setCurrency(donationRequest.getCurrency());
            newDonation.setDonationType(donationRequest.getDonationType());
            newDonation.setEventId(donationRequest.getEventId());
            newDonation.setMessage(donationRequest.getMessage());
            newDonation.setPaymentStatus("pending");

            // Anonim kontrol
            if (donationRequest.getIsAnonymous()) {
                newDonation.setIsAnonymous(true);
            } else {
                newDonation.setDonorName(donationRequest.getDonorName());
                newDonation.setDonorEmail(donationRequest.getDonorEmail());
                newDonation.setIsAnonymous(false);
            }

            Donation createdDonation = supabaseClient.insert("donations", newDonation, Donation.class);

            // Ödeme URL'si oluştur (Iyzico entegrasyonu buraya gelecek)
            Map<String, Object> response = new HashMap<>();
            response.put("donation", createdDonation);
            response.put("payment_url", "https://sandbox-api.iyzipay.com/payment/pay/" + createdDonation.getId());
            response.put("message", "Bağış oluşturuldu. Ödeme sayfasına yönlendiriliyorsunuz.");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bağış oluşturulamadı");
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        try {
            // Abonelik miktarının geçerli olduğunu kontrol et
            List<BigDecimal> allowedAmounts = subscriptionRequest.getCurrency().equals("TRY")
                    ? SUBSCRIPTION_AMOUNTS_TRY : SUBSCRIPTION_AMOUNTS_USD;

            if (!allowedAmounts.contains(subscriptionRequest.getAmount())) {
                return ResponseEntity.badRequest().body("Geçersiz abonelik miktarı");
            }

            // Abonelik objesini oluştur
            Subscription newSubscription = new Subscription();
            newSubscription.setAmount(subscriptionRequest.getAmount());
            newSubscription.setCurrency(subscriptionRequest.getCurrency());
            newSubscription.setMessage(subscriptionRequest.getMessage());
            newSubscription.setStatus("active");
            newSubscription.setNextPaymentDate(LocalDateTime.now().plusMonths(1));

            // Anonim kontrol
            if (subscriptionRequest.getIsAnonymous()) {
                newSubscription.setIsAnonymous(true);
            } else {
                newSubscription.setSubscriberName(subscriptionRequest.getSubscriberName());
                newSubscription.setSubscriberEmail(subscriptionRequest.getSubscriberEmail());
                newSubscription.setIsAnonymous(false);
            }

            Subscription createdSubscription = supabaseClient.insert("subscriptions", newSubscription, Subscription.class);

            // Ödeme URL'si oluştur (Iyzico recurring payment)
            Map<String, Object> response = new HashMap<>();
            response.put("subscription", createdSubscription);
            response.put("payment_url", "https://sandbox-api.iyzipay.com/subscription/pay/" + createdSubscription.getId());
            response.put("message", "Abonelik oluşturuldu. Ödeme sayfasına yönlendiriliyorsunuz.");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Abonelik oluşturulamadı");
        }
    }

    @GetMapping("/donations")
    public ResponseEntity<?> getDonations(@RequestParam(defaultValue = "general") String type,
                                          @RequestParam(required = false) String eventId,
                                          HttpServletRequest request) {
        try {
            String query = "donations?donation_type=eq." + type + "&payment_status=eq.completed&is_active=eq.true";

            if (eventId != null && !eventId.isEmpty()) {
                query += "&event_id=eq." + eventId;
            }

            // Sadece anonim olmayan bağışları göster (gizlilik için)
            query += "&is_anonymous=eq.false&order=created_at.desc";

            List<Donation> donations = supabaseClient.select(query, Donation.class);

            // Hassas bilgileri filtrele
            donations.forEach(donation -> {
                donation.setDonorEmail(null); // Email'i gösterme
                donation.setPaymentId(null);  // Payment ID'yi gösterme
            });

            return ResponseEntity.ok(donations);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bağışlar alınamadı");
        }
    }

    @PostMapping("/payment/callback")
    public ResponseEntity<?> handlePaymentCallback(@RequestBody PaymentCallbackRequest callbackRequest) {
        try {
            // Iyzico callback'ini işle
            if ("donation".equals(callbackRequest.getType())) {
                List<Donation> donations = supabaseClient.select("donations?id=eq." + callbackRequest.getResourceId(), Donation.class);
                if (!donations.isEmpty()) {
                    Donation donation = donations.get(0);
                    donation.setPaymentStatus(callbackRequest.getStatus());
                    donation.setPaymentId(callbackRequest.getPaymentId());
                    supabaseClient.update("donations", "id=eq." + callbackRequest.getResourceId(), donation, Donation.class);
                }
            } else if ("subscription".equals(callbackRequest.getType())) {
                List<Subscription> subscriptions = supabaseClient.select("subscriptions?id=eq." + callbackRequest.getResourceId(), Subscription.class);
                if (!subscriptions.isEmpty()) {
                    Subscription subscription = subscriptions.get(0);
                    subscription.setPaymentMethodId(callbackRequest.getPaymentId());
                    if ("failed".equals(callbackRequest.getStatus())) {
                        subscription.setStatus("cancelled");
                        subscription.setCancelledAt(LocalDateTime.now());
                    }
                    supabaseClient.update("subscriptions", "id=eq." + callbackRequest.getResourceId(), subscription, Subscription.class);
                }
            }

            return ResponseEntity.ok("Callback processed");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Callback işlenemedi");
        }
    }

    @PutMapping("/subscriptions/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable String subscriptionId,
                                                @RequestBody SubscriptionCancelRequest cancelRequest) {
        try {
            List<Subscription> subscriptions = supabaseClient.select("subscriptions?id=eq." + subscriptionId + "&subscriber_email=eq." + cancelRequest.getEmail(), Subscription.class);
            if (subscriptions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Abonelik bulunamadı");
            }

            Subscription subscription = subscriptions.get(0);
            subscription.setStatus("cancelled");
            subscription.setCancelledAt(LocalDateTime.now());

            Subscription updatedSubscription = supabaseClient.update("subscriptions", "id=eq." + subscriptionId, subscription, Subscription.class);

            return ResponseEntity.ok(updatedSubscription);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Abonelik iptal edilemedi");
        }
    }
}