package com.pwc.auditit.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for normalized Microsoft Entra ID user information.
 * Extracted from the ID token or userinfo endpoint response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntraUserInfoDto {
    
    /**
     * Subject claim - unique identifier for the user in Entra ID.
     * This is typically the user's OID (Object ID).
     */
    @JsonProperty("sub")
    private String subject;
    
    /**
     * User's email address (UPN - User Principal Name).
     */
    @JsonProperty("upn")
    private String email;
    
    /**
     * Alternative email claim.
     */
    @JsonProperty("email")
    private String alternativeEmail;
    
    /**
     * User's display name.
     */
    @JsonProperty("name")
    private String displayName;
    
    /**
     * User's first name.
     */
    @JsonProperty("given_name")
    private String givenName;
    
    /**
     * User's last name.
     */
    @JsonProperty("family_name")
    private String familyName;
    
    /**
     * Tenant ID if available.
     */
    @JsonProperty("tid")
    private String tenantId;
    
    /**
     * OID (Object ID) - another unique identifier.
     */
    @JsonProperty("oid")
    private String oid;
    
    /**
     * Gets the email, preferring upn over email claim.
     * 
     * @return the user's email address
     */
    public String getEmailAddress() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return alternativeEmail;
    }
    
    /**
     * Gets the unique external ID (OID if available, otherwise sub).
     * 
     * @return the external user ID
     */
    public String getExternalId() {
        if (oid != null && !oid.isBlank()) {
            return oid;
        }
        return subject;
    }
}

