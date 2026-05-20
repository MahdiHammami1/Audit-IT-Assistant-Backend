package com.pwc.auditit.controller;

import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/debug")
@Tag(name = "Authentication Debug", description = "Debug endpoints for authentication testing")
public class AuthDebugController {

    /**
     * Test endpoint to verify JWT token is being sent correctly
     */
    @GetMapping("/test-auth")
    @Operation(summary = "Test JWT authentication")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testAuth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> debugInfo = new HashMap<>();

        // Check if Authorization header is present
        debugInfo.put("authHeaderPresent", authHeader != null);
        if (authHeader != null) {
            debugInfo.put("authHeaderFormat", authHeader.startsWith("Bearer ") ? "Bearer token" : "Invalid format");
            debugInfo.put("tokenLength", authHeader.substring(7).length());
        }

        // Check SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        debugInfo.put("securityContextAuth", auth != null);
        if (auth != null) {
            debugInfo.put("authenticationPrincipal", auth.getPrincipal() != null ? auth.getPrincipal().getClass().getSimpleName() : "null");
            debugInfo.put("isAuthenticated", auth.isAuthenticated());
            debugInfo.put("authorities", auth.getAuthorities());
        }

        return ResponseEntity.ok(ApiResponse.ok("Auth debug info", debugInfo));
    }

    /**
     * Test endpoint with @CurrentUser annotation
     */
    @GetMapping("/test-current-user")
    @Operation(summary = "Test @CurrentUser annotation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testCurrentUser(
            @CurrentUser Profile currentUser) {

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("userId", currentUser.getId());
        result.put("email", currentUser.getEmail());
        result.put("fullName", currentUser.getFullName());

        return ResponseEntity.ok(ApiResponse.ok("Current user info", result));
    }

    /**
     * Test endpoint to verify CORS is working
     */
    @GetMapping("/test-cors")
    @Operation(summary = "Test CORS configuration")
    public ResponseEntity<ApiResponse<String>> testCors() {
        return ResponseEntity.ok(ApiResponse.ok("CORS is working"));
    }
}


