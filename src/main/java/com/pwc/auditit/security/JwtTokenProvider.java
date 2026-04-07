package com.pwc.auditit.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * JWT Token Provider - Wrapper around JwtService
 */
@Component
public class JwtTokenProvider {

    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(UUID userId, String email) {
        return jwtService.generateToken(userId, email);
    }

    public boolean isTokenValid(String token) {
        return jwtService.isTokenValid(token);
    }

    public UUID extractUserId(String token) {
        return jwtService.extractUserId(token);
    }
}

