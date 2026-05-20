package com.pwc.auditit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwc.auditit.config.EntraIdProperties;
import com.pwc.auditit.dto.response.*;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.entity.UserRole;
import com.pwc.auditit.entity.enums.AppRole;
import com.pwc.auditit.exception.AuthenticationException;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.repository.UserRoleRepository;
import com.pwc.auditit.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

/**
 * Service for handling Microsoft Entra ID authentication flow.
 *
 * This service manages:
 * - Fetching OpenID Connect metadata from Entra ID
 * - Building the authorization URL for login
 * - Exchanging authorization code for tokens
 * - Extracting and normalizing user information
 * - Creating/updating local user profiles
 * - Generating backend JWT tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntraIdAuthService {

    private final EntraIdProperties entraIdProperties;
    private final ProfileRepository profileRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private EntraIdMetadataDto cachedMetadata;
    private long metadataCacheTime;
    private static final long METADATA_CACHE_DURATION = 3600000; // 1 hour in milliseconds
    private static final String MICROSOFT_LOGIN_SCOPES = "openid profile email User.Read Files.Read offline_access";

    /**
     * Builds the Microsoft Entra ID authorization URL for user login.
     *
     * @return the authorization URL to redirect the user to
     * @throws AuthenticationException if Entra ID is not properly configured
     */
    public String getAuthorizationUrl() throws AuthenticationException {
        if (!entraIdProperties.isValid()) {
            log.error("Entra ID configuration is invalid or incomplete");
            throw new AuthenticationException("Entra ID is not properly configured");
        }

        try {
            EntraIdMetadataDto metadata = getMetadata();

            return UriComponentsBuilder.fromUriString(metadata.getAuthorizationEndpoint())
                    .queryParam("client_id", entraIdProperties.getClientId())
                    .queryParam("redirect_uri", entraIdProperties.getRedirectUri())
                    .queryParam("response_type", "code")
                    .queryParam("response_mode", "query")
                    .queryParam("scope", MICROSOFT_LOGIN_SCOPES)
                    .queryParam("state", generateState())
                    .build()
                    .toUriString();
        } catch (Exception e) {
            log.error("Failed to build authorization URL", e);
            throw new AuthenticationException("Failed to build authorization URL: " + e.getMessage());
        }
    }

    /**
     * Exchanges authorization code for tokens and extracts user information.
     *
     * @param code the authorization code from Entra ID callback
     * @return user information extracted from the ID token
     * @throws AuthenticationException if code exchange fails
     */
    public EntraUserInfoDto exchangeCodeForToken(String code) throws AuthenticationException {
        return exchangeCodeForTokenWithResponse(code).userInfo();
    }

    public EntraExchangeResult exchangeCodeForTokenWithResponse(String code) throws AuthenticationException {
        if (code == null || code.isBlank()) {
            log.warn("Authorization code is empty");
            throw new AuthenticationException("Authorization code is missing");
        }

        try {
            EntraIdMetadataDto metadata = getMetadata();

            // Exchange code for tokens
            EntraIdTokenResponseDto tokenResponse = exchangeCodeForTokens(code, metadata.getTokenEndpoint());

            // Extract user information from ID token
            EntraUserInfoDto userInfo = extractUserInfoFromIdToken(tokenResponse.getIdToken());

            log.info("Successfully exchanged code for token and extracted user info. Email: {}", userInfo.getEmailAddress());
            return new EntraExchangeResult(userInfo, tokenResponse);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to exchange authorization code for token", e);
            throw new AuthenticationException("Failed to complete authentication: " + e.getMessage());
        }
    }

    /**
     * Completes the authentication flow by:
     * 1. Finding or creating a local user profile
     * 2. Generating a backend JWT token
     * 3. Preparing the auth response
     *
     * @param entraUserInfo user information from Entra ID
     * @return authentication response with token and user info
     * @throws AuthenticationException if user provisioning fails
     */
    public EntraAuthResponseDto completeAuthentication(EntraUserInfoDto entraUserInfo)
            throws AuthenticationException {
        return completeAuthentication(entraUserInfo, null);
    }

    public EntraAuthResponseDto completeAuthentication(EntraUserInfoDto entraUserInfo, EntraIdTokenResponseDto tokenResponse)
            throws AuthenticationException {
        try {
            // Find or create local user profile
            Profile profile = findOrCreateProfile(entraUserInfo);
            saveGraphTokens(profile, tokenResponse);

            // Generate backend JWT token
            String jwtToken = jwtService.generateToken(profile.getId(), profile.getEmail());

            // Build response
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(profile.getId())
                    .email(profile.getEmail())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .fullName(profile.getFullName())
                    .build();

            EntraAuthResponseDto response = EntraAuthResponseDto.builder()
                    .token(jwtToken)
                    .user(userInfo)
                    .message("Authentication successful via Microsoft Entra ID")
                    .build();

            log.info("Authentication completed successfully for user: {}", profile.getEmail());
            return response;
        } catch (Exception e) {
            log.error("Failed to complete authentication", e);
            throw new AuthenticationException("Authentication completion failed: " + e.getMessage());
        }
    }

    /**
     * Fetches OpenID Connect metadata from Entra ID.
     * Results are cached for 1 hour to reduce network calls.
     *
     * @return metadata containing endpoints and configuration
     * @throws AuthenticationException if metadata fetch fails
     */
    private EntraIdMetadataDto getMetadata() throws AuthenticationException {
        // Return cached metadata if still valid
        if (cachedMetadata != null && (System.currentTimeMillis() - metadataCacheTime) < METADATA_CACHE_DURATION) {
            log.debug("Using cached Entra ID metadata");
            return cachedMetadata;
        }

        try {
            log.debug("Fetching Entra ID metadata from: {}", entraIdProperties.getMetadataUrl());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(entraIdProperties.getMetadataUrl()))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to fetch Entra ID metadata. Status: {}", response.statusCode());
                throw new AuthenticationException("Failed to fetch Entra ID configuration");
            }

            cachedMetadata = objectMapper.readValue(response.body(), EntraIdMetadataDto.class);
            metadataCacheTime = System.currentTimeMillis();

            log.debug("Successfully fetched and cached Entra ID metadata");
            return cachedMetadata;
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching Entra ID metadata", e);
            throw new AuthenticationException("Failed to fetch Entra ID metadata: " + e.getMessage());
        }
    }

    /**
     * Exchanges authorization code for tokens from Entra ID.
     *
     * @param code authorization code
     * @param tokenEndpoint Entra ID token endpoint
     * @return token response from Entra ID
     * @throws AuthenticationException if token exchange fails
     */
    private EntraIdTokenResponseDto exchangeCodeForTokens(String code, String tokenEndpoint)
            throws AuthenticationException {
        try {
            String requestBody = buildTokenExchangeRequestBody(code);

            log.debug("Exchanging authorization code for tokens at endpoint: {}", tokenEndpoint);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Token exchange failed. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new AuthenticationException("Failed to exchange code for token. Status: " + response.statusCode());
            }

            EntraIdTokenResponseDto tokenResponse = objectMapper.readValue(response.body(), EntraIdTokenResponseDto.class);

            if (tokenResponse.getIdToken() == null || tokenResponse.getIdToken().isBlank()) {
                log.error("No ID token in token response");
                throw new AuthenticationException("No ID token received from Entra ID");
            }

            log.debug("Successfully exchanged code for tokens");
            return tokenResponse;
        } catch (AuthenticationException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            log.error("Error during token exchange", e);
            throw new AuthenticationException("Token exchange failed: " + e.getMessage());
        }
    }

    /**
     * Builds the request body for token exchange.
     *
     * @param code authorization code
     * @return URL-encoded request body
     */
    private String buildTokenExchangeRequestBody(String code) {
        return UriComponentsBuilder.newInstance()
                .queryParam("client_id", entraIdProperties.getClientId())
                .queryParam("client_secret", entraIdProperties.getClientSecret())
                .queryParam("code", code)
                .queryParam("redirect_uri", entraIdProperties.getRedirectUri())
                .queryParam("grant_type", "authorization_code")
                .queryParam("scope", MICROSOFT_LOGIN_SCOPES)
                .build()
                .getQuery();
    }

    /**
     * Extracts user information from the ID token (JWT).
     * Note: In production, you might want to validate the token signature.
     * For now, we trust Entra ID's signature verification via HTTPS.
     *
     * @param idToken the ID token (JWT format)
     * @return extracted user information
     * @throws AuthenticationException if token parsing fails
     */
    private EntraUserInfoDto extractUserInfoFromIdToken(String idToken)
            throws AuthenticationException {
        try {
            if (idToken == null || idToken.isBlank()) {
                log.error("ID token is null or blank");
                throw new AuthenticationException("ID token is missing or empty");
            }

            // JWT format: header.payload.signature
            // We only need the payload (middle part)
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT format. Expected 3 parts, got {}", parts.length);
                throw new AuthenticationException("Invalid JWT format");
            }

            // Decode the payload (second part) from Base64URL
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String jsonPayload = new String(decoder.decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);

            log.debug("Decoded JWT payload");

            // Parse JSON to extract claims
            @java.lang.SuppressWarnings("unchecked")
            java.util.Map<String, Object> claimsMap = objectMapper.readValue(jsonPayload, java.util.Map.class);

             EntraUserInfoDto userInfo = EntraUserInfoDto.builder()
                    .subject((String) claimsMap.get("sub"))
                    .email((String) claimsMap.get("upn"))
                    .alternativeEmail((String) claimsMap.get("email"))
                    .displayName((String) claimsMap.get("name"))
                    .givenName((String) claimsMap.get("given_name"))
                    .familyName((String) claimsMap.get("family_name"))
                    .oid((String) claimsMap.get("oid"))
                    .tenantId((String) claimsMap.get("tid"))
                    .build();

            if (userInfo.getEmailAddress() == null || userInfo.getEmailAddress().isBlank()) {
                log.error("No email found in Entra ID token. Claims: {}", claimsMap.keySet());
                throw new AuthenticationException("Email not found in user information");
            }

            log.info("Successfully extracted user info from ID token. Email: {}", userInfo.getEmailAddress());
            return userInfo;
        } catch (AuthenticationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 encoding in JWT", e);
            throw new AuthenticationException("Failed to decode token: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to extract user info from ID token", e);
            throw new AuthenticationException("Failed to parse user information: " + e.getMessage());
        }
    }

    /**
     * Safely extracts a claim from JWT claims.
     *
     * @param claims JWT claims
     * @param key claim key
     * @return claim value or null if not found
     */
    private String extractClaim(Claims claims, String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Finds an existing profile by Entra ID external ID, or creates a new one.
     *
     * Strategy:
     * 1. Try to find user by email (email is unique)
     * 2. If not found, create a new profile with Entra ID information
     * 3. Update existing profile with latest Entra ID information
     * 4. Assign default role (auditor) to new users
     *
     * @param entraUserInfo user information from Entra ID
     * @return the local user profile
     */
    private Profile findOrCreateProfile(EntraUserInfoDto entraUserInfo) {
        String email = entraUserInfo.getEmailAddress();

        // Try to find existing profile by email
        Optional<Profile> existingProfile = profileRepository.findByEmail(email);

        if (existingProfile.isPresent()) {
            Profile profile = existingProfile.get();
            // Update profile with latest information from Entra ID
            profile.setFirstName(entraUserInfo.getGivenName());
            profile.setLastName(entraUserInfo.getFamilyName());
            profile.setFullName(entraUserInfo.getDisplayName());
            // Mark as verified since they authenticated via Entra ID
            profile.setVerified(true);

            profileRepository.save(profile);
            log.info("Updated existing profile for email: {}", email);
            return profile;
        }

        // Create new profile
        log.info("Creating new profile for Entra ID user (Sign-up flow): {}", email);
        Profile newProfile = Profile.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName(entraUserInfo.getGivenName())
                .lastName(entraUserInfo.getFamilyName())
                .fullName(entraUserInfo.getDisplayName())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        // Mark as verified since they authenticated via Entra ID
        newProfile.setVerified(true);

        // Save the new profile first
        Profile savedProfile = profileRepository.save(newProfile);
        log.info("New user profile created successfully for Entra ID user: {} (ID: {})", email, savedProfile.getId());

        // Assign default role (auditor) to the new user
        try {
            UserRole defaultRole = UserRole.builder()
                    .id(UUID.randomUUID())
                    .user(savedProfile)
                    .role(AppRole.auditor)
                    .build();

            userRoleRepository.save(defaultRole);
            log.info("Default role 'auditor' assigned to new user: {}", email);

            // Reload profile with roles
            savedProfile.getRoles().add(defaultRole);
        } catch (Exception e) {
            log.error("Failed to assign default role to user {}: {}", email, e.getMessage());
            // Continue even if role assignment fails, user is still created
        }

        return savedProfile;
    }

    private void saveGraphTokens(Profile profile, EntraIdTokenResponseDto tokenResponse) {
        if (profile == null || tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            return;
        }

        long expiresIn = tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : 3600L;
        profile.setGraphAccessToken(tokenResponse.getAccessToken());
        profile.setGraphTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
        profile.setGraphConnectedAt(Instant.now());

        if (tokenResponse.getRefreshToken() != null && !tokenResponse.getRefreshToken().isBlank()) {
            profile.setGraphRefreshToken(tokenResponse.getRefreshToken());
        }

        profileRepository.save(profile);
        log.info("Stored Microsoft Graph delegated token for profile {}", profile.getId());
    }

    /**
     * Generates a random state for CSRF protection during OAuth flow.
     *
     * @return random state string
     */
    private String generateState() {
        return UUID.randomUUID().toString();
    }

    public record EntraExchangeResult(EntraUserInfoDto userInfo, EntraIdTokenResponseDto tokenResponse) {
    }
}









