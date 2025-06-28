package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.*;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sparks")
public class SparkController {

    @Autowired
    private SupabaseClient supabaseClient;

    // Etkileşim kartları listesi
    private final List<String> INTERACTION_CARDS = Arrays.asList(
            "Yap!", "Bekle", "Yapma", "Pivot et",
            "Para odaklı düşün", "Kalbini dinle", "Veri topla", "Sezgini kullan",
            "Katılıyorum", "Katılmıyorum", "Düşündürdü", "Perspektif değiştirdi",
            "Dikkatli ol", "Riskli ama değer", "Güvenli git", "Fırsat kaçırma",
            "Devam et", "Hızlan", "Sabırlı ol", "Odaklan"
    );

    @GetMapping
    public ResponseEntity<?> getAllSparks(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        try {
            int offset = page * size;
            String query = "sparks?is_active=eq.true&order=created_at.desc&limit=" + size + "&offset=" + offset;
            List<Object> sparks = supabaseClient.select(query, Object.class);

            return ResponseEntity.ok(sparks);
        } catch (IOException e) {
            System.err.println("❌ Get All Sparks Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/{sparkId}")
    public ResponseEntity<?> getSparkById(@PathVariable String sparkId) {
        try {
            List<Object> sparks = supabaseClient.select("sparks?id=eq." + sparkId + "&is_active=eq.true", Object.class);
            if (sparks.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(sparks.get(0));
        } catch (IOException e) {
            System.err.println("❌ Get Spark By ID Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createSpark(@RequestBody SparkCreateRequest createRequest,
                                         HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Kullanıcı bilgilerini al (Object olarak)
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users.get(0);
            String fullName = (String) user.get("full_name");

            // Spark objesini oluştur
            Spark newSpark = new Spark();
            newSpark.setUserId(userId);
            newSpark.setAuthorName(fullName != null ? fullName : "Bilinmeyen Kullanıcı");
            newSpark.setContent(createRequest.getContent());
            newSpark.setAllowCollaboration(createRequest.getAllowCollaboration());

            Object createdSpark = supabaseClient.insert("sparks", newSpark, Object.class);

            return ResponseEntity.ok(createdSpark);
        } catch (IOException e) {
            System.err.println("❌ Create Spark Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/{sparkId}/interactions")
    public ResponseEntity<?> getSparkInteractions(@PathVariable String sparkId) {
        try {
            // Spark'ın var olduğunu kontrol et
            List<Object> sparks = supabaseClient.select("sparks?id=eq." + sparkId + "&is_active=eq.true", Object.class);
            if (sparks.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Spark'ın etkileşimlerini al
            String query = "spark_interactions?spark_id=eq." + sparkId + "&is_active=eq.true&order=created_at.desc";
            List<Object> interactions = supabaseClient.select(query, Object.class);

            // Etkileşimleri grupla
            Map<String, Integer> interactionCounts = new HashMap<>();
            for (Object interactionObj : interactions) {
                @SuppressWarnings("unchecked")
                Map<String, Object> interaction = (Map<String, Object>) interactionObj;
                String type = (String) interaction.get("interaction_type");
                if (type != null) {
                    interactionCounts.put(type, interactionCounts.getOrDefault(type, 0) + 1);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("interactions", interactions);
            response.put("counts", interactionCounts);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("❌ Get Spark Interactions Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/{sparkId}/interact")
    public ResponseEntity<?> interactWithSpark(@PathVariable String sparkId,
                                               @RequestBody SparkInteractionRequest interactionRequest,
                                               HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Debug logları ekle
            System.out.println("🔍 Received interaction type: [" + interactionRequest.getInteractionType() + "]");
            System.out.println("🔍 Available cards: " + INTERACTION_CARDS);
            System.out.println("🔍 Contains check: " + INTERACTION_CARDS.contains(interactionRequest.getInteractionType()));

            // Spark'ın var olduğunu kontrol et
            List<Object> sparks = supabaseClient.select("sparks?id=eq." + sparkId + "&is_active=eq.true", Object.class);
            if (sparks.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Etkileşim tipinin geçerli olduğunu kontrol et
            if (!INTERACTION_CARDS.contains(interactionRequest.getInteractionType())) {
                System.out.println("❌ Geçersiz etkileşim tipi: [" + interactionRequest.getInteractionType() + "]");
                return ResponseEntity.badRequest().body("Geçersiz etkileşim tipi: " + interactionRequest.getInteractionType());
            }

            // Kullanıcının daha önce bu tipteki etkileşimi yapıp yapmadığını kontrol et
            String checkQuery = "spark_interactions?spark_id=eq." + sparkId +
                    "&user_id=eq." + userId +
                    "&interaction_type=eq." + interactionRequest.getInteractionType() +
                    "&is_active=eq.true";
            List<Object> existingInteractions = supabaseClient.select(checkQuery, Object.class);

            if (!existingInteractions.isEmpty()) {
                return ResponseEntity.badRequest().body("Bu etkileşim tipini daha önce kullandınız");
            }

            // Kullanıcı bilgilerini al
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users.get(0);
            String fullName = (String) user.get("full_name");

            // Etkileşim objesini oluştur
            SparkInteraction newInteraction = new SparkInteraction();
            newInteraction.setSparkId(sparkId);
            newInteraction.setUserId(userId);
            newInteraction.setAuthorName(fullName != null ? fullName : "Bilinmeyen Kullanıcı");
            newInteraction.setInteractionType(interactionRequest.getInteractionType());

            Object createdInteraction = supabaseClient.insert("spark_interactions", newInteraction, Object.class);

            return ResponseEntity.ok(createdInteraction);
        } catch (IOException e) {
            System.err.println("❌ Interact With Spark Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/interaction-cards")
    public ResponseEntity<?> getInteractionCards() {
        return ResponseEntity.ok(INTERACTION_CARDS);
    }

    @PutMapping("/{sparkId}/settings")
    public ResponseEntity<?> updateSparkSettings(@PathVariable String sparkId,
                                                 @RequestBody SparkSettingsRequest settingsRequest,
                                                 HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Spark'ın var olduğunu ve kullanıcıya ait olduğunu kontrol et
            List<Object> sparks = supabaseClient.select("sparks?id=eq." + sparkId + "&user_id=eq." + userId + "&is_active=eq.true", Object.class);
            if (sparks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu spark'ı düzenleme yetkiniz yok");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> sparkData = (Map<String, Object>) sparks.get(0);

            Spark spark = new Spark();
            spark.setAllowCollaboration(settingsRequest.getAllowCollaboration());

            Object updatedSpark = supabaseClient.update("sparks", "id=eq." + sparkId, spark, Object.class);

            return ResponseEntity.ok(updatedSpark);
        } catch (IOException e) {
            System.err.println("❌ Update Spark Settings Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }
}