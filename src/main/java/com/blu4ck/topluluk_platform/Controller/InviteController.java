package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.Invite.InviteCode;
import com.blu4ck.topluluk_platform.Model.Invite.InviteRequest;
import com.blu4ck.topluluk_platform.Service.InviteService;
import com.blu4ck.topluluk_platform.Util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invites")
public class InviteController {

    @Autowired
    private InviteService inviteService;

    @Autowired
    private ResponseUtil responseUtil;

    @PostMapping("/send")
    public ResponseEntity<?> sendInvitations(@Valid @RequestBody InviteRequest inviteRequest,
                                             HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return responseUtil.unauthorized();
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Kullanıcının davet etme yetkisi var mı kontrol et
            if (!inviteService.canUserInviteOthers(userId)) {
                return ResponseEntity.badRequest().body("Davet etme yetkiniz bulunmuyor");
            }

            List<String> successfulInvites = new ArrayList<>();
            List<String> failedInvites = new ArrayList<>();

            for (String email : inviteRequest.getEmailAddresses()) {
                try {
                    String inviteCode = inviteService.generateInviteCode(
                            userId,
                            email,
                            inviteRequest.getInviteType(),
                            inviteRequest.getMessage()
                    );
                    successfulInvites.add(email);
                    System.out.println("✅ Davet gönderildi: " + email + " - Kod: " + inviteCode);
                } catch (Exception e) {
                    failedInvites.add(email + " (" + e.getMessage() + ")");
                    System.err.println("❌ Davet gönderilemedi: " + email + " - " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successful", successfulInvites);
            result.put("failed", failedInvites);
            result.put("total_sent", successfulInvites.size());

            return responseUtil.success("invite.sent", result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Davet gönderme hatası: " + e.getMessage());
        }
    }

    @GetMapping("/my-invites")
    public ResponseEntity<?> getMyInvites(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return responseUtil.unauthorized();
        }

        String userId = (String) request.getAttribute("userId");

        try {
            List<InviteCode> invites = inviteService.getUserInvites(userId);
            return responseUtil.success(invites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Davetler alınamadı: " + e.getMessage());
        }
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateInviteCode(@PathVariable String code) {
        try {
            InviteCode inviteCode = inviteService.validateInviteCode(code);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("inviter_name", inviteCode.getInviterName());
            result.put("expires_at", inviteCode.getExpiresAt());
            result.put("can_invite_others", inviteCode.getCanInviteOthers());

            return responseUtil.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/can-invite")
    public ResponseEntity<?> canInviteOthers(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return responseUtil.unauthorized();
        }

        String userId = (String) request.getAttribute("userId");

        try {
            boolean canInvite = inviteService.canUserInviteOthers(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("can_invite", canInvite);

            return responseUtil.success(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Yetki kontrolü yapılamadı: " + e.getMessage());
        }
    }
}