package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.*;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessagingController {

    @Autowired
    private SupabaseClient supabaseClient;

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Kullanıcının tüm konuşmalarını al
            String query = "conversations?or=(user1_id.eq." + userId + ",user2_id.eq." + userId + ")&is_active=eq.true&order=last_message_at.desc";
            List<Conversation> conversations = supabaseClient.select(query, Conversation.class);

            return ResponseEntity.ok(conversations);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Konuşmalar alınamadı");
        }
    }

    @PostMapping("/conversations")
    public ResponseEntity<?> startConversation(@RequestBody ConversationRequest conversationRequest,
                                               HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Hedef kullanıcının var olduğunu kontrol et
            List<User> targetUsers = supabaseClient.select("users?id=eq." + conversationRequest.getTargetUserId(), User.class);
            if (targetUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hedef kullanıcı bulunamadı");
            }

            // Mevcut konuşma var mı kontrol et
            String existingQuery = "conversations?or=(and(user1_id.eq." + userId + ",user2_id.eq." + conversationRequest.getTargetUserId() + "),and(user1_id.eq." + conversationRequest.getTargetUserId() + ",user2_id.eq." + userId + "))&is_active=eq.true";
            List<Conversation> existingConversations = supabaseClient.select(existingQuery, Conversation.class);

            if (!existingConversations.isEmpty()) {
                return ResponseEntity.ok(existingConversations.get(0));
            }

            // Kullanıcı bilgilerini al
            List<User> currentUsers = supabaseClient.select("users?id=eq." + userId, User.class);
            if (currentUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            User currentUser = currentUsers.get(0);
            User targetUser = targetUsers.get(0);

            // Yeni konuşma oluştur
            Conversation newConversation = new Conversation();
            newConversation.setUser1Id(userId);
            newConversation.setUser2Id(conversationRequest.getTargetUserId());
            newConversation.setUser1Name(currentUser.getFullName());
            newConversation.setUser2Name(targetUser.getFullName());
            newConversation.setConversationType("general");
            newConversation.setLastMessageAt(LocalDateTime.now());

            Conversation createdConversation = supabaseClient.insert("conversations", newConversation, Conversation.class);

            return ResponseEntity.ok(createdConversation);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Konuşma başlatılamadı");
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable String conversationId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "50") int size,
                                                     HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Konuşmanın var olduğunu ve kullanıcının yetkili olduğunu kontrol et
            String conversationQuery = "conversations?id=eq." + conversationId + "&or=(user1_id.eq." + userId + ",user2_id.eq." + userId + ")&is_active=eq.true";
            List<Conversation> conversations = supabaseClient.select(conversationQuery, Conversation.class);

            if (conversations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu konuşmaya erişim yetkiniz yok");
            }

            // Mesajları al
            int offset = page * size;
            String messageQuery = "messages?conversation_id=eq." + conversationId + "&is_active=eq.true&order=created_at.desc&limit=" + size + "&offset=" + offset;
            List<Message> messages = supabaseClient.select(messageQuery, Message.class);

            return ResponseEntity.ok(messages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mesajlar alınamadı");
        }
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String conversationId,
                                         @RequestBody MessageRequest messageRequest,
                                         HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Konuşmanın var olduğunu ve kullanıcının yetkili olduğunu kontrol et
            String conversationQuery = "conversations?id=eq." + conversationId + "&or=(user1_id.eq." + userId + ",user2_id.eq." + userId + ")&is_active=eq.true";
            List<Conversation> conversations = supabaseClient.select(conversationQuery, Conversation.class);

            if (conversations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu konuşmaya mesaj gönderme yetkiniz yok");
            }

            // Kullanıcı bilgilerini al
            List<User> users = supabaseClient.select("users?id=eq." + userId, User.class);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            User user = users.get(0);

            // Yeni mesaj oluştur
            Message newMessage = new Message();
            newMessage.setConversationId(conversationId);
            newMessage.setSenderId(userId);
            newMessage.setSenderName(user.getFullName());
            newMessage.setContent(messageRequest.getContent());

            Message createdMessage = supabaseClient.insert("messages", newMessage, Message.class);

            // Konuşmanın son mesaj bilgilerini güncelle
            Conversation conversation = conversations.get(0);
            conversation.setLastMessage(messageRequest.getContent());
            conversation.setLastMessageAt(LocalDateTime.now());
            supabaseClient.update("conversations", "id=eq." + conversationId, conversation, Conversation.class);

            return ResponseEntity.ok(createdMessage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mesaj gönderilemedi");
        }
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable String messageId,
                                               HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Mesajın var olduğunu kontrol et
            List<Message> messages = supabaseClient.select("messages?id=eq." + messageId, Message.class);
            if (messages.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Message message = messages.get(0);

            // Mesajın alıcısının bu kullanıcı olduğunu kontrol et (sender değil)
            if (message.getSenderId().equals(userId)) {
                return ResponseEntity.badRequest().body("Kendi mesajınızı okundu olarak işaretleyemezsiniz");
            }

            message.setIsRead(true);
            Message updatedMessage = supabaseClient.update("messages", "id=eq." + messageId, message, Message.class);

            return ResponseEntity.ok(updatedMessage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mesaj okundu olarak işaretlenemedi");
        }
    }
}