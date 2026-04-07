package com.pwc.auditit.controller;

import com.pwc.auditit.dto.request.*;
import com.pwc.auditit.dto.response.AuthResponse;
import com.pwc.auditit.dto.response.AuthMessageResponse;
import com.pwc.auditit.dto.response.ApiResponse;
import com.pwc.auditit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173"})
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Sign up endpoint
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthMessageResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Received sign up request for email: {}", request.getEmail());
        try {
            AuthMessageResponse response = authService.signUp(request);
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("User registered successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error during sign up: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Sign up failed: " + e.getMessage()));
        }
    }

    /**
     * Sign in endpoint
     * POST /api/auth/signin
     * Returns: JWT Token + User Info
     */
    @PostMapping("/signin")
    @Operation(
        summary = "Sign in / Login",
        description = "Authenticate user with email and password. Returns JWT token and user information."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sign in successful - JWT token returned",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid email or password"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> signIn(@Valid @RequestBody SignInRequest request) {
        log.info("Received sign in request for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.signIn(request);
            log.info("Sign in successful for email: {}. JWT token generated.", request.getEmail());
            return ResponseEntity.ok(ApiResponse.ok("Sign in successful", response));
        } catch (Exception e) {
            log.error("Error during sign in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Sign in failed: " + e.getMessage()));
        }
    }

    /**
     * Verify code endpoint
     * POST /api/auth/verify-code
     */
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<AuthMessageResponse>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("Received verify code request for email: {}", request.getEmail());
        try {
            AuthMessageResponse response = authService.verifyCode(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.ok("Email verified successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error during code verification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Verification failed: " + e.getMessage()));
        }
    }

    /**
     * Forgot password endpoint
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<AuthMessageResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Received forgot password request for email: {}", request.getEmail());
        try {
            AuthMessageResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.ok("Password reset code sent", response));
        } catch (Exception e) {
            log.error("Error during forgot password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Forgot password failed: " + e.getMessage()));
        }
    }

    /**
     * Reset password endpoint
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthMessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Received reset password request for email: {}", request.getEmail());
        try {
            AuthMessageResponse response = authService.resetPassword(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error during password reset: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Password reset failed: " + e.getMessage()));
        }
    }

    /**
     * Resend verification code endpoint
     * POST /api/auth/resend-code
     */
    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse<AuthMessageResponse>> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        log.info("Received resend code request for email: {}", request.getEmail());
        try {
            AuthMessageResponse response = authService.resendCode(request);
            return ResponseEntity.ok(ApiResponse.ok("Verification code sent", response));
        } catch (Exception e) {
            log.error("Error during resend code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Resend code failed: " + e.getMessage()));
        }
    }
}

