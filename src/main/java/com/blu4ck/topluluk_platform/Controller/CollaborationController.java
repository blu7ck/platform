package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.Collaboration;
import com.blu4ck.topluluk_platform.Model.CollaborationRequest;
import com.blu4ck.topluluk_platform.Model.User;
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
@RequestMapping("/collaborations")
public class CollaborationController {

    @Autowired
    private SupabaseClient supabaseClient;

    @GetMapping
    public ResponseEntity<?> getUserCollaborations(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Kullanıcının başlattığı veya katıldığı işbirlikleri
            String query = "collaborations?or=(initiator_id.eq." + userId + ",participant_id.eq." + userId + ")&is_active=eq.true&order=created_at.desc";
            List<Collaboration> collaborations = supabaseClient.select(query, Collaboration.class);

            return ResponseEntity.ok(collaborations);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşbirlikleri alınamadı");
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestCollaboration(@RequestBody CollaborationRequest collaborationRequest,
                                                  HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String initiatorId = (String) request.getAttribute("userId");

        try {
            // Başlatıcı kullanıcı bilgilerini al
            List<User> initiatorUsers = supabaseClient.select("users?id=eq." + initiatorId, User.class);
            if (initiatorUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            // Katılımcı kullanıcı bilgilerini al
            List<User> participantUsers = supabaseClient.select("users?id=eq." + collaborationRequest.getParticipantId(), User.class);
            if (participantUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hedef kullanıcı bulunamadı");
            }

            User initiator = initiatorUsers.get(0);
            User participant = participantUsers.get(0);

            // Kendisiyle işbirliği engelle
            if (initiatorId.equals(collaborationRequest.getParticipantId())) {
                return ResponseEntity.badRequest().body("Kendinizle işbirliği yapamazsınız");
            }

            // Mevcut aktif işbirliği kontrolü
            String existingQuery = "collaborations?initiator_id=eq." + initiatorId +
                    "&participant_id=eq." + collaborationRequest.getParticipantId() +
                    "&status=in.(pending,approved,active)&is_active=eq.true";
            List<Collaboration> existingCollaborations = supabaseClient.select(existingQuery, Collaboration.class);

            if (!existingCollaborations.isEmpty()) {
                return ResponseEntity.badRequest().body("Bu kullanıcıyla zaten aktif bir işbirliğiniz var");
            }

            // Yeni işbirliği oluştur
            Collaboration newCollaboration = new Collaboration();
            newCollaboration.setInitiatorId(initiatorId);
            newCollaboration.setParticipantId(collaborationRequest.getParticipantId());
            newCollaboration.setInitiatorName(initiator.getFullName());
            newCollaboration.setParticipantName(participant.getFullName());
            newCollaboration.setSourceType(collaborationRequest.getSourceType());
            newCollaboration.setSourceId(collaborationRequest.getSourceId());
            newCollaboration.setTitle(collaborationRequest.getTitle());
            newCollaboration.setDescription(collaborationRequest.getDescription());
            newCollaboration.setStatus("pending");

            Collaboration createdCollaboration = supabaseClient.insert("collaborations", newCollaboration, Collaboration.class);

            return ResponseEntity.ok(createdCollaboration);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşbirliği talebi gönderilemedi");
        }
    }

    @PutMapping("/{collaborationId}/approve")
    public ResponseEntity<?> approveCollaboration(@PathVariable String collaborationId,
                                                  HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // İşbirliğini al ve kullanıcının katılımcı olduğunu kontrol et
            List<Collaboration> collaborations = supabaseClient.select("collaborations?id=eq." + collaborationId + "&participant_id=eq." + userId + "&status=eq.pending", Collaboration.class);
            if (collaborations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu işbirliğini onaylama yetkiniz yok");
            }

            Collaboration collaboration = collaborations.get(0);
            collaboration.setStatus("approved");

            Collaboration updatedCollaboration = supabaseClient.update("collaborations", "id=eq." + collaborationId, collaboration, Collaboration.class);

            return ResponseEntity.ok(updatedCollaboration);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşbirliği onaylanamadı");
        }
    }

    @PutMapping("/{collaborationId}/start")
    public ResponseEntity<?> startCollaboration(@PathVariable String collaborationId,
                                                HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // İşbirliğini al ve kullanıcının katılımcı olduğunu kontrol et
            String query = "collaborations?id=eq." + collaborationId +
                    "&or=(initiator_id.eq." + userId + ",participant_id.eq." + userId + ")" +
                    "&status=eq.approved";
            List<Collaboration> collaborations = supabaseClient.select(query, Collaboration.class);

            if (collaborations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu işbirliğini başlatma yetkiniz yok");
            }

            Collaboration collaboration = collaborations.get(0);
            collaboration.setStatus("active");

            Collaboration updatedCollaboration = supabaseClient.update("collaborations", "id=eq." + collaborationId, collaboration, Collaboration.class);

            // Kullanıcıların işbirliği sayılarını artır
            updateUserCollaborationCount(collaboration.getInitiatorId());
            updateUserCollaborationCount(collaboration.getParticipantId());

            return ResponseEntity.ok(updatedCollaboration);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşbirliği başlatılamadı");
        }
    }

    @PutMapping("/{collaborationId}/cancel")
    public ResponseEntity<?> cancelCollaboration(@PathVariable String collaborationId,
                                                 HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // İşbirliğini al ve kullanıcının yetkili olduğunu kontrol et
            String query = "collaborations?id=eq." + collaborationId +
                    "&or=(initiator_id.eq." + userId + ",participant_id.eq." + userId + ")" +
                    "&status=in.(pending,approved)";
            List<Collaboration> collaborations = supabaseClient.select(query, Collaboration.class);

            if (collaborations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu işbirliğini iptal etme yetkiniz yok");
            }

            Collaboration collaboration = collaborations.get(0);
            collaboration.setStatus("cancelled");

            Collaboration updatedCollaboration = supabaseClient.update("collaborations", "id=eq." + collaborationId, collaboration, Collaboration.class);

            return ResponseEntity.ok(updatedCollaboration);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşbirliği iptal edilemedi");
        }
    }

    @PostMapping("/{collaborationId}/request-premium")
    public ResponseEntity<?> requestPremiumService(@PathVariable String collaborationId,
                                                   HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // İşbirliğini al ve kullanıcının yetkili olduğunu kontrol et
            String query = "collaborations?id=eq." + collaborationId +
                    "&or=(initiator_id.eq." + userId + ",participant_id.eq." + userId + ")" +
                    "&status=eq.active";
            List<Collaboration> collaborations = supabaseClient.select(query, Collaboration.class);

            if (collaborations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu işbirliği için premium hizmet talep edemezsiniz");
            }

            // Premium hizmet talebini e-posta ile bildirme (şimdilik basit response)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Premium hizmet talebiniz alındı. En kısa sürede iletişime geçeceğiz.");
            response.put("collaborationId", collaborationId);
            response.put("estimatedPrice", "200 USD per participant");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Premium hizmet talebi gönderilemedi");
        }
    }

    private void updateUserCollaborationCount(String userId) throws IOException {
        List<User> users = supabaseClient.select("users?id=eq." + userId, User.class);
        if (!users.isEmpty()) {
            User user = users.get(0);
            user.setCollaborationCount(user.getCollaborationCount() + 1);
            supabaseClient.update("users", "id=eq." + userId, user, User.class);
        }
    }
}