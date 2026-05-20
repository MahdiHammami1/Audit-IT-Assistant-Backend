package com.pwc.auditit.exception;

/**
 * Exception thrown when authentication fails or authentication-related operations encounters errors.
 * Examples:
 * - Invalid credentials
 * - Token validation fails
 * - Entra ID configuration missing
 * - OAuth2 code exchange fails
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

