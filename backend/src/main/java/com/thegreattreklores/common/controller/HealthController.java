package com.thegreattreklores.common.controller;

import com.thegreattreklores.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "System health check endpoints")
public class HealthController {

    @Value("${spring.application.name:the-great-trek-lores}")
    private String appName;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @GetMapping
    @Operation(summary = "Health Check", description = "Returns system status, profile, and server timestamp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("status", "UP");
        healthData.put("application", appName);
        healthData.put("profile", activeProfile);
        healthData.put("version", "1.0.0-MVP");
        healthData.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(ApiResponse.success("System is operational", healthData));
    }
}
