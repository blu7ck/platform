package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.Register.RegisterRequest;
import com.blu4ck.topluluk_platform.Model.Invite.InviteCode;
import com.blu4ck.topluluk_platform.Service.InviteService;
import com.blu4ck.topluluk_platform.Service.MailService;
import com.blu4ck.topluluk_platform.Util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private InviteService inviteService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ResponseUtil responseUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Davet kodunu doğrula
            InviteCode inviteCode = inviteService.validateInviteCode(registerRequest.getInviteCode());

            // Davet edilen email ile kayıt olan email eşleşmeli
            if (!inviteCode.getInvitedEmail().equals(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Bu davet kodu sizin e-posta adresiniz için değil");
            }

            // Supabase'de kullanıcı oluştur (bu kısım Supabase Auth ile yapılacak)
            // Şimdilik mock response döndürüyoruz
            
            // Başarılı kayıt sonrası davet kodunu kullanılmış olarak işaretle
            // inviteService.markInviteCodeAsUsed(registerRequest.getInviteCode(), userId, registerRequest.getEmail());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Kayıt başarılı! Giriş yapabilirsiniz.");
            result.put("can_invite_others", inviteCode.getCanInviteOthers());

            return responseUtil.success("user.registered", result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kayıt hatası: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("E-posta adresi gerekli");
        }

        try {
            // Şifre sıfırlama token'ı oluştur (Supabase Auth ile yapılacak)
            String resetToken = "mock-reset-token-" + System.currentTimeMillis();
            
            // Mail gönder
            mailService.sendPasswordResetEmail(email, resetToken, "Kullanıcı");

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Şifre sıfırlama linki e-posta adresinize gönderildi");

            return responseUtil.success(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Şifre sıfırlama hatası: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token ve yeni şifre gerekli");
        }

        try {
            // Token doğrulama ve şifre güncelleme (Supabase Auth ile yapılacak)
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Şifreniz başarıyla güncellendi");

            return responseUtil.success(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Şifre sıfırlama hatası: " + e.getMessage());
        }
    }
}