package com.pwc.auditit.controller;

import com.pwc.auditit.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur de test pour les emails
 * Endpoints:
 * - GET /api/test/send-email?to=email@example.com&subject=Test&body=Test%20body
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    /**
     * Envoyer un email de test
     * GET /api/test/send-email?to=user@example.com&subject=Test&body=Ceci est un test
     */
    @GetMapping("/send-email")
    public ResponseEntity<?> sendTestEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            emailService.sendSimpleEmail(to, subject, body);
            return ResponseEntity.ok(new Response(true, "Email envoyé avec succès à: " + to));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false, "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Envoyer un email HTML de test
     * GET /api/test/send-html-email?to=user@example.com&subject=Test HTML
     */
    @GetMapping("/send-html-email")
    public ResponseEntity<?> sendTestHtmlEmail(
            @RequestParam String to,
            @RequestParam String subject
    ) {
        try {
            String htmlBody = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                            .header { background-color: #1e40af; color: white; padding: 20px; border-radius: 5px; }
                            .content { padding: 20px; border: 1px solid #e5e7eb; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>Email de Test HTML</h1>
                            </div>
                            <div class="content">
                                <p>Ceci est un email HTML de test.</p>
                                <p>Si vous recevez cet email, votre configuration SMTP est correcte!</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            emailService.sendHtmlEmail(to, subject, htmlBody);
            return ResponseEntity.ok(new Response(true, "Email HTML envoyé avec succès à: " + to));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email HTML de test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false, "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Envoyer un email d'activation de compte de test
     * GET /api/test/send-activation-email?to=user@example.com&username=testuser
     */
    @GetMapping("/send-activation-email")
    public ResponseEntity<?> sendTestActivationEmail(
            @RequestParam String to,
            @RequestParam String username
    ) {
        try {
            String activationLink = "http://localhost:5173/activate?token=test-token-12345";
            emailService.sendAccountActivationEmail(to, username, activationLink);
            return ResponseEntity.ok(new Response(true, "Email d'activation envoyé avec succès à: " + to));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'activation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false, "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Envoyer un email de réinitialisation de mot de passe de test
     * GET /api/test/send-reset-password-email?to=user@example.com&username=testuser
     */
    @GetMapping("/send-reset-password-email")
    public ResponseEntity<?> sendTestResetPasswordEmail(
            @RequestParam String to,
            @RequestParam String username
    ) {
        try {
            String resetLink = "http://localhost:5173/reset-password?token=test-token-12345";
            emailService.sendPasswordResetEmail(to, username, resetLink);
            return ResponseEntity.ok(new Response(true, "Email de réinitialisation envoyé avec succès à: " + to));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false, "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Réponse simple pour les tests
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class Response {
        private boolean success;
        private String message;
    }
}

