package com.blu4ck.topluluk_platform.Supabase;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SupabaseClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Value("${supabase.service-key}")
    private String serviceKey;

    public SupabaseClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        System.out.println("🔧 SupabaseClient initialized");

    }

    public <T> List<T> select(String table, Class<T> responseType) throws IOException {
        String url = supabaseUrl + "/rest/v1/" + table;

        System.out.println("🌐 Supabase URL: " + url);
        System.out.println("🔑 Anon Key: " + (anonKey != null ? anonKey.substring(0, 20) + "..." : "NULL"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, responseType));


        }
    }

    public <T> T insert(String table, Object data, Class<T> responseType) throws IOException {
        String url = supabaseUrl + "/rest/v1/" + table;
        String jsonData = objectMapper.writeValueAsString(data);

        System.out.println("🔍 INSERT Request:");
        System.out.println("📍 URL: " + url);
        System.out.println("📝 JSON Data: " + jsonData);

        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + serviceKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println("📊 Response Code: " + response.code());
            String responseBody = response.body().string();
            System.out.println("📋 Response Body: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("Insert failed: " + response.code() + " - " + responseBody);
            }

            List<T> results = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, responseType));
            return results.isEmpty() ? null : results.get(0);
        }
    }

    public <T> T update(String table, String filter, Object data, Class<T> responseType) throws IOException {
        String url = supabaseUrl + "/rest/v1/" + table + "?" + filter;
        String jsonData = objectMapper.writeValueAsString(data);

        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + serviceKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Update failed: " + response.code());
            }

            String responseBody = response.body().string();
            List<T> results = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, responseType));
            return results.isEmpty() ? null : results.get(0);
        }
    }

    public boolean delete(String table, String filter) throws IOException {
        String url = supabaseUrl + "/rest/v1/" + table + "?" + filter;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + serviceKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }
}
