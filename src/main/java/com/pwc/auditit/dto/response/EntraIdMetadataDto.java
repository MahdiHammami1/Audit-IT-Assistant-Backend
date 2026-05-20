package com.pwc.auditit.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Microsoft Entra ID OpenID Connect metadata.
 * Retrieved from the metadata URL (typically ends with .well-known/openid-configuration).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntraIdMetadataDto {
    
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;
    
    @JsonProperty("token_endpoint")
    private String tokenEndpoint;
    
    @JsonProperty("userinfo_endpoint")
    private String userinfoEndpoint;
    
    @JsonProperty("jwks_uri")
    private String jwksUri;
    
    @JsonProperty("issuer")
    private String issuer;
}

