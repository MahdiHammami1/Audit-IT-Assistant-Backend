package com.pwc.auditit.service;

import com.pwc.auditit.exception.MicrosoftGraphException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GraphOAuthStateStore {

    private static final long STATE_TTL_SECONDS = 600;

    private final Map<String, PendingGraphOAuthState> pendingStates = new ConcurrentHashMap<>();

    public String createState(UUID userId) {
        return createState(userId, "/cdw-generation");
    }

    public String createState(UUID userId, String returnPath) {
        String state = UUID.randomUUID().toString();
        pendingStates.put(state, new PendingGraphOAuthState(userId, sanitizeReturnPath(returnPath), Instant.now().plusSeconds(STATE_TTL_SECONDS)));
        return state;
    }

    public ConsumedGraphOAuthState consumeState(String state) {
        PendingGraphOAuthState pendingState = pendingStates.remove(state);
        if (pendingState == null || pendingState.expiresAt().isBefore(Instant.now())) {
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Microsoft Graph connection state is missing or expired.");
        }
        return new ConsumedGraphOAuthState(pendingState.userId(), pendingState.returnPath());
    }

    public void validateState(String state) {
        PendingGraphOAuthState pendingState = pendingStates.get(state);
        if (pendingState == null || pendingState.expiresAt().isBefore(Instant.now())) {
            pendingStates.remove(state);
            throw new MicrosoftGraphException(HttpStatus.BAD_REQUEST, "Microsoft Graph connection state is missing or expired.");
        }
    }

    public String getReturnPathOrDefault(String state) {
        if (state == null || state.isBlank()) {
            return "/cdw-generation";
        }
        PendingGraphOAuthState pendingState = pendingStates.get(state);
        if (pendingState == null || pendingState.expiresAt().isBefore(Instant.now())) {
            pendingStates.remove(state);
            return "/cdw-generation";
        }
        return pendingState.returnPath();
    }

    private String sanitizeReturnPath(String returnPath) {
        if (returnPath == null || returnPath.isBlank() || !returnPath.startsWith("/") || returnPath.startsWith("//")) {
            return "/cdw-generation";
        }
        return returnPath;
    }

    public record ConsumedGraphOAuthState(UUID userId, String returnPath) {
    }

    private record PendingGraphOAuthState(UUID userId, String returnPath, Instant expiresAt) {
    }
}
