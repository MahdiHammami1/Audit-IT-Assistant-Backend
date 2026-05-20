package com.pwc.auditit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "graph")
@Data
public class GraphProperties {

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String delegatedScopes;
    private String redirectUri;

    public boolean isConfigured() {
        return hasText(tenantId)
                && hasText(clientId)
                && hasText(clientSecret)
                && hasText(baseUrl)
                && hasText(delegatedScopes)
                && hasText(redirectUri);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
