package com.pwc.auditit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwc.auditit.config.EntraIdProperties;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.dto.response.AuthResponse;
import com.pwc.auditit.dto.response.EntraAuthResponseDto;
import com.pwc.auditit.exception.AuthenticationException;
import com.pwc.auditit.service.EntraIdAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller for Microsoft Entra ID authentication endpoints.
 *
 * This controller handles the OAuth2 flow for Entra ID:
 * 1. GET /api/entra/login -> Redirect to Microsoft Entra ID
 * 2. GET /api/entra/callback -> Receive authorization code and complete auth
 * 3. POST /api/entra/logout -> Clear authentication
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Microsoft Entra ID Authentication", description = "Microsoft Entra ID (Azure AD) OAuth2 authentication endpoints")
public class AuthEntraController {

    private final EntraIdAuthService entraIdAuthService;
    private final EntraIdProperties entraIdProperties;
    private final ObjectMapper objectMapper;

    /**
     * Initiates the login flow by redirecting to Microsoft Entra ID.
     *
     * @return authorization URL to redirect the user to
     */
    @GetMapping("/entra/login")
    @Operation(
        summary = "Initiate Entra ID login",
        description = "Returns the Microsoft Entra ID authorization URL. Client should redirect user to this URL."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Authorization URL generated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Entra ID is not properly configured"
        )
    })
    public ResponseEntity<ApiResponse<EntraLoginUrlResponse>> getLoginUrl() {
        try {
            String authUrl = entraIdAuthService.getAuthorizationUrl();
            EntraLoginUrlResponse response = new EntraLoginUrlResponse(authUrl);
            return ResponseEntity.ok(
                ApiResponse.ok("Entra ID login URL generated", response)
            );
        } catch (AuthenticationException e) {
            log.error("Failed to generate login URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate login URL: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login URL generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Callback endpoint from Microsoft Entra ID.
     * Receives the authorization code and redirects to frontend with JWT token.
     *
     * Frontend usage:
     * 1. User logs in via Microsoft Entra ID
     * 2. Entra ID redirects to: /api/auth/callback?code=xxx&state=yyy
     * 3. Backend processes the code and redirects to frontend with token
     * 4. Frontend extracts token from URL and stores it
     *
     * @param code authorization code from Entra ID
     * @param state state parameter for CSRF protection
     * @return redirect to frontend with token parameter
     */
    @GetMapping("/callback")
    @Operation(
        summary = "Entra ID OAuth2 callback",
        description = "Processes the authorization code from Entra ID and redirects to frontend with JWT token."
    )
    public ResponseEntity<Void> callback(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "state", required = false) String state,
        @RequestParam(value = "error", required = false) String providerError,
        @RequestParam(value = "error_description", required = false) String providerErrorDescription
    ) {
        if (hasText(providerError) || hasText(providerErrorDescription)) {
            String message = providerErrorDescription != null ? providerErrorDescription : providerError;
            return redirectToFrontendWithError(message);
        }

        if (!hasText(code)) {
            return redirectToFrontendWithError("Authorization code is missing");
        }

        try {
            // Step 1: Exchange code for tokens
            EntraIdAuthService.EntraExchangeResult exchangeResult = entraIdAuthService.exchangeCodeForTokenWithResponse(code);

            // Step 2: Complete authentication (find/create user, generate JWT)
            EntraAuthResponseDto authResponse = entraIdAuthService.completeAuthentication(
                    exchangeResult.userInfo(),
                    exchangeResult.tokenResponse()
            );

            if (authResponse == null || !hasText(authResponse.getToken())) {
                log.error("Entra authentication completed without a JWT token");
                return redirectToFrontendWithError("Authentication token is missing");
            }

            // Step 3: Redirect to the frontend Microsoft callback page with token/user query params
            String frontendUrl = buildFrontendSuccessUrl(authResponse);

            return redirectToFrontend(frontendUrl);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            return redirectToFrontendWithError("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during callback processing", e);
            return redirectToFrontendWithError("Authentication error: " + e.getMessage());
        }
    }

    private String buildFrontendSuccessUrl(EntraAuthResponseDto authResponse) throws JsonProcessingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(entraIdProperties.getFrontendCallbackUri())
                .queryParam("token", authResponse.getToken());

        AuthResponse.UserInfo user = authResponse.getUser();
        if (user != null) {
            builder.queryParam("user", objectMapper.writeValueAsString(user));
            addQueryParamIfPresent(builder, "id", user.getId());
            addQueryParamIfPresent(builder, "email", user.getEmail());
            addQueryParamIfPresent(builder, "firstName", user.getFirstName());
            addQueryParamIfPresent(builder, "lastName", user.getLastName());
            addQueryParamIfPresent(builder, "fullName", user.getFullName());
        }

        return builder.build()
                .encode()
                .toUriString();
    }

    private ResponseEntity<Void> redirectToFrontendWithError(String message) {
        String frontendUrl = UriComponentsBuilder.fromUriString(entraIdProperties.getFrontendCallbackUri())
                .queryParam("error", "authentication_failed")
                .queryParam("error_description", message)
                .build()
                .encode()
                .toUriString();

        return redirectToFrontend(frontendUrl);
    }

    private ResponseEntity<Void> redirectToFrontend(String frontendUrl) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl)
                .build();
    }

    private void addQueryParamIfPresent(UriComponentsBuilder builder, String name, Object value) {
        if (value != null && hasText(value.toString())) {
            builder.queryParam(name, value);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Logout endpoint.
     * Clears the client-side JWT token.
     *
     * Note: Since we're using stateless JWT authentication on the backend,
     * logout happens entirely on the client side. The client simply discards
     * the JWT token. If you want to implement token blacklisting, you'd need
     * to maintain a blacklist on the backend.
     *
     * Optionally, you can call the Entra ID logout endpoint from the frontend
     * to sign the user out of Microsoft services as well.
     *
     * @return logout response
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout user",
        description = "Logs out the authenticated user. Stateless auth means JWT token becomes invalid on client."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful"
        )
    })
    public ResponseEntity<ApiResponse<Object>> logout() {
        try {
            log.info("User logout requested");
            // In a stateless JWT setup, logout is purely client-side (discard token)
            // You could also implement token blacklisting here if needed
            return ResponseEntity.ok(
                ApiResponse.ok("Logout successful", null)
            );
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Logout failed: " + e.getMessage()));
        }
    }

    /**
     * Simple DTO for login URL response.
     */
    public record EntraLoginUrlResponse(
        String loginUrl
    ) {} 
}

