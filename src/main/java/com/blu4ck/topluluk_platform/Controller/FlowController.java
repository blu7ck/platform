package com.blu4ck.topluluk_platform.Controller;

import com.blu4ck.topluluk_platform.Model.*;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flows")
public class FlowController {

    @Autowired
    private SupabaseClient supabaseClient;

    @GetMapping
    public ResponseEntity<?> getAllFlows(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        try {
            int offset = page * size;
            String query = "flows?is_active=eq.true&order=created_at.desc&limit=" + size + "&offset=" + offset;
            List<Object> flows = supabaseClient.select(query, Object.class);

            return ResponseEntity.ok(flows);
        } catch (IOException e) {
            System.err.println("❌ Get All Flows Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/{flowId}")
    public ResponseEntity<?> getFlowById(@PathVariable String flowId) {
        try {
            List<Object> flows = supabaseClient.select("flows?id=eq." + flowId + "&is_active=eq.true", Object.class);
            if (flows.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(flows.get(0));
        } catch (IOException e) {
            System.err.println("❌ Get Flow By ID Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createFlow(@RequestBody FlowCreateRequest createRequest,
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

            // Flow objesini oluştur
            Flow newFlow = new Flow();
            newFlow.setUserId(userId);
            newFlow.setAuthorName(fullName != null ? fullName : "Bilinmeyen Kullanıcı");
            newFlow.setTitle(createRequest.getTitle());
            newFlow.setContent(createRequest.getContent());
            newFlow.setAllowCollaboration(createRequest.getAllowCollaboration());
            newFlow.setAllowSharing(createRequest.getAllowSharing());

            Object createdFlow = supabaseClient.insert("flows", newFlow, Object.class);

            return ResponseEntity.ok(createdFlow);
        } catch (IOException e) {
            System.err.println("❌ Create Flow Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/{flowId}/responses")
    public ResponseEntity<?> getFlowResponses(@PathVariable String flowId) {
        try {
            // Flow'un var olduğunu kontrol et
            List<Object> flows = supabaseClient.select("flows?id=eq." + flowId + "&is_active=eq.true", Object.class);
            if (flows.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Flow'un cevaplarını al
            String query = "flow_responses?flow_id=eq." + flowId + "&is_active=eq.true&order=created_at.asc";
            List<Object> responses = supabaseClient.select(query, Object.class);

            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            System.err.println("❌ Get Flow Responses Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/{flowId}/responses")
    public ResponseEntity<?> respondToFlow(@PathVariable String flowId,
                                           @RequestBody FlowResponseRequest responseRequest,
                                           HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Flow'un var olduğunu kontrol et
            List<Object> flows = supabaseClient.select("flows?id=eq." + flowId + "&is_active=eq.true", Object.class);
            if (flows.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Kullanıcının daha önce bu flow'a cevap verip vermediğini kontrol et
            String checkQuery = "flow_responses?flow_id=eq." + flowId + "&user_id=eq." + userId + "&is_active=eq.true";
            List<Object> existingResponses = supabaseClient.select(checkQuery, Object.class);

            if (!existingResponses.isEmpty()) {
                return ResponseEntity.badRequest().body("Bu flow'a daha önce cevap verdiniz");
            }

            // Kullanıcı bilgilerini al
            List<Object> users = supabaseClient.select("users?id=eq." + userId, Object.class);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) users.get(0);
            String fullName = (String) user.get("full_name");

            // Cevap objesini oluştur
            FlowResponse newResponse = new FlowResponse();
            newResponse.setFlowId(flowId);
            newResponse.setUserId(userId);
            newResponse.setAuthorName(fullName != null ? fullName : "Bilinmeyen Kullanıcı");
            newResponse.setContent(responseRequest.getContent());

            Object createdResponse = supabaseClient.insert("flow_responses", newResponse, Object.class);

            // Flow'un cevap sayısını artır (basit update)
            return ResponseEntity.ok(createdResponse);
        } catch (IOException e) {
            System.err.println("❌ Respond To Flow Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PutMapping("/{flowId}/settings")
    public ResponseEntity<?> updateFlowSettings(@PathVariable String flowId,
                                                @RequestBody FlowSettingsRequest settingsRequest,
                                                HttpServletRequest request) {
        Boolean isAuthenticated = (Boolean) request.getAttribute("isAuthenticated");
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş yapmalısınız");
        }

        String userId = (String) request.getAttribute("userId");

        try {
            // Flow'un var olduğunu ve kullanıcıya ait olduğunu kontrol et
            List<Object> flows = supabaseClient.select("flows?id=eq." + flowId + "&user_id=eq." + userId + "&is_active=eq.true", Object.class);
            if (flows.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu flow'u düzenleme yetkiniz yok");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> flowData = (Map<String, Object>) flows.get(0);

            Flow flow = new Flow();
            flow.setAllowCollaboration(settingsRequest.getAllowCollaboration());
            flow.setAllowSharing(settingsRequest.getAllowSharing());

            Object updatedFlow = supabaseClient.update("flows", "id=eq." + flowId, flow, Object.class);

            return ResponseEntity.ok(updatedFlow);
        } catch (IOException e) {
            System.err.println("❌ Update Flow Settings Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }
}