package com.blu4ck.topluluk_platform.Auth;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class AuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public UserInfo validateTokenAndGetUser(String authToken) {
        try {
            String url = supabaseUrl + "/auth/v1/user";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + authToken)
                    .addHeader("apikey", anonKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("🔴 Auth validation failed: " + response.code());
                    return null;
                }

                String responseBody = response.body().string();
                JsonNode userNode = objectMapper.readTree(responseBody);

                String userId = userNode.get("id").asText();
                String email = userNode.get("email").asText();

                System.out.println("✅ Auth validation successful for user: " + email);

                return new UserInfo(userId, email);
            }
        } catch (Exception e) {
            System.out.println("❌ Auth error: " + e.getMessage());
            return null;
        }
    }

    // Inner class for user info
    public static class UserInfo {
        private final String userId;
        private final String email;

        public UserInfo(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
    }
}