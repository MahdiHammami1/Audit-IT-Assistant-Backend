package com.pwc.auditit.service.impl;

import com.pwc.auditit.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.from-name:AuditIT}")
    private String fromName;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendEmailToMultiple(String[] recipients, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipients);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to {} recipients", recipients.length);
        } catch (Exception e) {
            log.error("Error sending email to multiple recipients: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendAccountActivationEmail(String to, String username, String activationLink) {
        try {
            String htmlContent = buildAccountActivationEmailContent(username, activationLink);
            sendHtmlEmail(to, "Activate your AuditIT account", htmlContent);
            log.info("Activation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending activation email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending activation email: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        try {
            String htmlContent = buildPasswordResetEmailContent(username, resetLink);
            sendHtmlEmail(to, "Reset your AuditIT password", htmlContent);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending password reset email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending password reset email: " + e.getMessage());
        }
    }

    @Override
    public void sendNotificationEmail(String to, String subject, String message) {
        try {
            String htmlContent = buildNotificationEmailContent(message);
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Notification sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending notification: " + e.getMessage());
        }
    }

    private String buildAccountActivationEmailContent(String username, String activationLink) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #1e40af; color: white; padding: 20px; border-radius: 5px;'>" +
                "<h1>Welcome to AuditIT</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #e5e7eb;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Thank you for signing up! To start using AuditIT, please activate your account by clicking the button below:</p>" +
                "<p><a href='" + activationLink + "' style='display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #1e40af; color: white; text-decoration: none; border-radius: 5px;'>Activate Account</a></p>" +
                "<p>Or copy and paste this link in your browser:</p>" +
                "<p>" + activationLink + "</p>" +
                "<p>This link expires in 24 hours.</p>" +
                "</div>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #6b7280;'>" +
                "<p>If you did not create this account, please ignore this email.</p>" +
                "<p>&copy; 2026 AuditIT by PwC. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordResetEmailContent(String username, String resetLink) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #dc2626; color: white; padding: 20px; border-radius: 5px;'>" +
                "<h1>Reset Your Password</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #e5e7eb;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>You have requested to reset your password. Click the button below to set a new password:</p>" +
                "<p><a href='" + resetLink + "' style='display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #dc2626; color: white; text-decoration: none; border-radius: 5px;'>Reset Password</a></p>" +
                "<p>Or copy and paste this link:</p>" +
                "<p>" + resetLink + "</p>" +
                "<p>This link expires in 1 hour.</p>" +
                "<p><strong>Important:</strong> If you did not request this reset, please ignore this email and contact our support.</p>" +
                "</div>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #6b7280;'>" +
                "<p>&copy; 2026 AuditIT by PwC. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildNotificationEmailContent(String message) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #059669; color: white; padding: 20px; border-radius: 5px;'>" +
                "<h1>AuditIT Notification</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #e5e7eb;'>" +
                message +
                "</div>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #6b7280;'>" +
                "<p>&copy; 2026 AuditIT by PwC. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

