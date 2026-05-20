package com.pwc.auditit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Microsoft Entra ID (Azure AD) integration.
 * 
 * These properties are populated from environment variables via application.yml/properties:
 * - auth.entra.client-id -> AUTH_ENTRA_CLIENT_ID
 * - auth.entra.client-secret -> AUTH_ENTRA_CLIENT_SECRET
 * - auth.entra.redirect-uri -> AUTH_ENTRA_REDIRECT_URI
 * - auth.entra.frontend-callback-uri -> AUTH_ENTRA_FRONTEND_CALLBACK_URI
 * - auth.entra.post-logout-redirect-uri -> AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI
 * - auth.entra.metadata-url -> AUTH_ENTRA_METADATA_URL
 * - auth.entra.tenant-id -> AUTH_ENTRA_TENANT_ID (optional)
 */
@Component
@ConfigurationProperties(prefix = "auth.entra")
@Data
public class EntraIdProperties {
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String frontendCallbackUri;
    private String postLogoutRedirectUri;
    private String metadataUrl;
    private String tenantId;
    
    /**
     * Validates that all required properties are configured.
     * 
     * @return true if all required properties are set
     */
    public boolean isValid() {
        return clientId != null && !clientId.isBlank() &&
               clientSecret != null && !clientSecret.isBlank() &&
               redirectUri != null && !redirectUri.isBlank() &&
               metadataUrl != null && !metadataUrl.isBlank();
    }
}

