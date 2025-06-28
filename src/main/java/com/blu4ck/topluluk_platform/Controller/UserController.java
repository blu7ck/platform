package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.User;
import com.blu4ck.topluluk_platform.Model.UserUpdateRequest;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private SupabaseClient supabaseClient;

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Geçici: Raw JSON olarak dön (JSON parsing sorununu bypass et)
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(users.get(0));
        } catch (IOException e) {
            System.err.println("❌ Hata detayı: " + e.getMessage());
            e.printStackTrace(); // Tam stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest updateRequest,
                                           HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            User updatedUser = supabaseClient.update(
                    "users",
                    "id=eq." + userId,
                    updateRequest,
                    User.class
            );

            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            System.err.println("❌ Profile Update Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/invite-status")
    public ResponseEntity<?> getInviteStatus(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Geçici: Raw JSON olarak al
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Raw JSON'dan değerleri al (geçici çözüm)
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users.get(0);

            Integer inviteQuota = (Integer) user.get("invite_quota");
            Integer usedInvites = (Integer) user.get("used_invites");

            if (inviteQuota == null) inviteQuota = 5;
            if (usedInvites == null) usedInvites = 0;

            Map<String, Object> inviteStatus = new HashMap<>();
            inviteStatus.put("quota", inviteQuota);
            inviteStatus.put("used", usedInvites);
            inviteStatus.put("remaining", inviteQuota - usedInvites);

            return ResponseEntity.ok(inviteStatus);
        } catch (IOException e) {
            System.err.println("❌ Invite Status Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/generate-invite")
    public ResponseEntity<?> generateInviteCode(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Kullanıcının kalan davet hakkını kontrol et
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users.get(0);

            Integer inviteQuota = (Integer) user.get("invite_quota");
            Integer usedInvites = (Integer) user.get("used_invites");

            if (inviteQuota == null) inviteQuota = 5;
            if (usedInvites == null) usedInvites = 0;

            if (usedInvites >= inviteQuota) {
                return ResponseEntity.badRequest().body("Davet hakkınız kalmamış");
            }

            // Yeni davet kodu oluştur (şimdilik basit)
            String inviteCode = generateRandomCode();

            // Davet kodunu kaydet ve kullanılan davet sayısını artır
            // TODO: InviteCodeModel oluşturup kaydet

            Map<String, String> response = new HashMap<>();
            response.put("inviteCode", inviteCode);
            response.put("message", "Davet kodu oluşturuldu");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("❌ Generate Invite Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    private String generateRandomCode() {
        // Basit random kod oluşturucu
        return "INV" + System.currentTimeMillis();
    }
}