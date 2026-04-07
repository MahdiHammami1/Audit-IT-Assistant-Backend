package com.pwc.auditit.service;

public interface EmailService {
    /**
     * Envoyer un email simple
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Envoyer un email HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Envoyer un email à plusieurs destinataires
     */
    void sendEmailToMultiple(String[] recipients, String subject, String body);

    /**
     * Envoyer un email d'activation de compte
     */
    void sendAccountActivationEmail(String to, String username, String activationLink);

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    void sendPasswordResetEmail(String to, String username, String resetLink);

    /**
     * Envoyer une notification
     */
    void sendNotificationEmail(String to, String subject, String message);
}

