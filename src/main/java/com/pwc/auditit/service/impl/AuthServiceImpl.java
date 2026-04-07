package com.pwc.auditit.service.impl;

import com.pwc.auditit.dto.request.*;
import com.pwc.auditit.dto.response.AuthResponse;
import com.pwc.auditit.dto.response.AuthMessageResponse;
import com.pwc.auditit.entity.Profile;
import com.pwc.auditit.exception.ResourceNotFoundException;
import com.pwc.auditit.repository.ProfileRepository;
import com.pwc.auditit.security.JwtTokenProvider;
import com.pwc.auditit.service.AuthService;
import com.pwc.auditit.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private static final int VERIFICATION_CODE_LENGTH = 6;

    @Override
    public AuthMessageResponse signUp(SignUpRequest request) {
        log.info("Processing sign up for email: {}", request.getEmail());

        // Check if email already exists
        if (profileRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already registered: {}", request.getEmail());
            return AuthMessageResponse.builder()
                .success(false)
                .message("Email already registered")
                .build();
        }

        // Generate verification code
        String verificationCode = generateVerificationCode();

        // Create new profile
        Profile profile = Profile.builder()
            .id(UUID.randomUUID())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .fullName(request.getFirstName() + " " + request.getLastName())
            .password(passwordEncoder.encode(request.getPassword()))
            .verificationCode(verificationCode)
            .isVerified(false)
            .build();

        profileRepository.save(profile);
        log.info("User registered successfully: {}", profile.getEmail());

        // TODO: Send verification code to email
        sendVerificationCodeEmail(request.getEmail(), verificationCode);

        return AuthMessageResponse.builder()
            .success(true)
            .message("Sign up successful. Please verify your email.")
            .build();
    }

    @Override
    public AuthResponse signIn(SignInRequest request) {
        log.info("Processing sign in for email: {}", request.getEmail());

        Profile profile = profileRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getEmail());
                return new ResourceNotFoundException("User not found");
            });

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            throw new ResourceNotFoundException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(profile.getId(), profile.getEmail());

        log.info("User signed in successfully: {}", profile.getEmail());

        return AuthResponse.builder()
            .token(token)
            .user(AuthResponse.UserInfo.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .build())
            .build();
    }

    @Override
    public AuthMessageResponse verifyCode(VerifyCodeRequest request) {
        log.info("Processing code verification for email: {}", request.getEmail());

        Profile profile = profileRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getEmail());
                return new ResourceNotFoundException("User not found");
            });

        // Check verification code
        if (!request.getCode().equals(profile.getVerificationCode())) {
            log.warn("Invalid verification code for user: {}", request.getEmail());
            return AuthMessageResponse.builder()
                .success(false)
                .message("Invalid verification code")
                .build();
        }

        // Mark user as verified
        profile.setVerified(true);
        profile.setVerificationCode(null);
        profileRepository.save(profile);

        log.info("Email verified successfully: {}", profile.getEmail());

        return AuthMessageResponse.builder()
            .success(true)
            .message("Email verified successfully")
            .build();
    }

    @Override
    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password for email: {}", request.getEmail());

        Profile profile = profileRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getEmail());
                return new ResourceNotFoundException("User not found");
            });

        // Generate reset code
        String resetCode = generateVerificationCode();
        profile.setVerificationCode(resetCode);
        profileRepository.save(profile);

        log.info("Forgot password request processed for: {}", request.getEmail());

        // TODO: Send reset code to email
        sendPasswordResetEmail(request.getEmail(), resetCode);

        return AuthMessageResponse.builder()
            .success(true)
            .message("Password reset code sent to your email")
            .build();
    }

    @Override
    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset for email: {}", request.getEmail());

        Profile profile = profileRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getEmail());
                return new ResourceNotFoundException("User not found");
            });

        // Verify reset code
        if (!request.getCode().equals(profile.getVerificationCode())) {
            log.warn("Invalid reset code for user: {}", request.getEmail());
            return AuthMessageResponse.builder()
                .success(false)
                .message("Invalid reset code")
                .build();
        }

        // Update password
        profile.setPassword(passwordEncoder.encode(request.getNewPassword()));
        profile.setVerificationCode(null);
        profileRepository.save(profile);

        log.info("Password reset successfully: {}", profile.getEmail());

        return AuthMessageResponse.builder()
            .success(true)
            .message("Password reset successfully")
            .build();
    }

    @Override
    public AuthMessageResponse resendCode(ResendCodeRequest request) {
        log.info("Processing resend code for email: {}", request.getEmail());

        Profile profile = profileRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getEmail());
                return new ResourceNotFoundException("User not found");
            });

        // Generate new verification code
        String verificationCode = generateVerificationCode();
        profile.setVerificationCode(verificationCode);
        profileRepository.save(profile);

        log.info("Verification code resent for: {}", request.getEmail());

        // TODO: Send verification code to email
        sendVerificationCodeEmail(request.getEmail(), verificationCode);

        return AuthMessageResponse.builder()
            .success(true)
            .message("Verification code sent to your email")
            .build();
    }

    /**
     * Generate a random 6-digit verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Send verification code email
     */
    private void sendVerificationCodeEmail(String email, String code) {
        try {
            String subject = "Vérifiez votre adresse email - AuditIT";
            String htmlBody = buildVerificationCodeEmailContent(code);
            emailService.sendHtmlEmail(email, subject, htmlBody);
            log.info("Verification code email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Error sending verification code email to {}: {}", email, e.getMessage(), e);
            // Don't throw exception, just log it - the user can still verify later or resend
        }
    }

    /**
     * Send password reset email
     */
    private void sendPasswordResetEmail(String email, String code) {
        try {
            String subject = "Réinitialiser votre mot de passe - AuditIT";
            String htmlBody = buildPasswordResetEmailContent(code);
            emailService.sendHtmlEmail(email, subject, htmlBody);
            log.info("Password reset code email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Error sending password reset email to {}: {}", email, e.getMessage(), e);
            // Don't throw exception, just log it - the user can still reset later or resend
        }
    }

    /**
     * Build HTML content for verification code email
     */
    private String buildVerificationCodeEmailContent(String code) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #1e40af; color: white; padding: 20px; border-radius: 5px; text-align: center;'>" +
                "<h1>AuditIT - Vérification d'Email</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #e5e7eb;'>" +
                "<p>Bonjour,</p>" +
                "<p>Merci de vous être inscrit sur AuditIT. Voici votre code de vérification:</p>" +
                "<div style='background-color: #f3f4f6; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;'>" +
                "<p style='font-size: 32px; font-weight: bold; color: #1e40af; letter-spacing: 5px; margin: 0;'>" + code + "</p>" +
                "</div>" +
                "<p>Entrez ce code dans l'application pour vérifier votre adresse email.</p>" +
                "<p><strong>Important:</strong> Ce code expire dans 24 heures.</p>" +
                "<p>Si vous n'avez pas créé ce compte, veuillez ignorer cet email.</p>" +
                "</div>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #6b7280; text-align: center;'>" +
                "<p>&copy; 2026 AuditIT by PwC. Tous les droits réservés.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailContent(String code) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #dc2626; color: white; padding: 20px; border-radius: 5px; text-align: center;'>" +
                "<h1>AuditIT - Réinitialiser votre mot de passe</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #e5e7eb;'>" +
                "<p>Bonjour,</p>" +
                "<p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code de réinitialisation:</p>" +
                "<div style='background-color: #fef2f2; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;'>" +
                "<p style='font-size: 32px; font-weight: bold; color: #dc2626; letter-spacing: 5px; margin: 0;'>" + code + "</p>" +
                "</div>" +
                "<p>Entrez ce code dans l'application pour réinitialiser votre mot de passe.</p>" +
                "<p><strong>Important:</strong> Ce code expire dans 1 heure.</p>" +
                "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email et contacter notre support immédiatement.</p>" +
                "</div>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #6b7280; text-align: center;'>" +
                "<p>&copy; 2026 AuditIT by PwC. Tous les droits réservés.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

