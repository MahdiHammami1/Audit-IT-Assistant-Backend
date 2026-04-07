package com.pwc.auditit.service;

import com.pwc.auditit.dto.request.*;
import com.pwc.auditit.dto.response.AuthResponse;
import com.pwc.auditit.dto.response.AuthMessageResponse;

public interface AuthService {

    /**
     * Register a new user
     */
    AuthMessageResponse signUp(SignUpRequest request);

    /**
     * Authenticate user with email and password
     */
    AuthResponse signIn(SignInRequest request);

    /**
     * Verify user email with verification code
     */
    AuthMessageResponse verifyCode(VerifyCodeRequest request);

    /**
     * Request password reset
     */
    AuthMessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password with verification code
     */
    AuthMessageResponse resetPassword(ResetPasswordRequest request);

    /**
     * Resend verification code to email
     */
    AuthMessageResponse resendCode(ResendCodeRequest request);
}

