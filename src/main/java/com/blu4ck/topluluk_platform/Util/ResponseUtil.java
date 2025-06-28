package com.blu4ck.topluluk_platform.Util;

import com.blu4ck.topluluk_platform.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseUtil {

    @Autowired
    private MessageService messageService;

    public ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> success(String messageKey, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", messageService.getMessage(messageKey));
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> error(HttpStatus status, String messageKey) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", messageService.getMessage(messageKey));
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    public ResponseEntity<Map<String, Object>> error(HttpStatus status, String messageKey, Object... args) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", messageService.getMessage(messageKey, args));
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    public ResponseEntity<Map<String, Object>> unauthorized() {
        return error(HttpStatus.UNAUTHORIZED, "app.error.unauthorized");
    }

    public ResponseEntity<Map<String, Object>> forbidden() {
        return error(HttpStatus.FORBIDDEN, "app.error.forbidden");
    }

    public ResponseEntity<Map<String, Object>> notFound() {
        return error(HttpStatus.NOT_FOUND, "app.error.notfound");
    }
}