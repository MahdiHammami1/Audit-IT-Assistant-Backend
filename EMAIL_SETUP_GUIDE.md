# 📧 Guide Configuration Email - AuditIT

## Configuration SMTP

### Variables d'Environnement Requises

```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_STARTTLS_REQUIRED=true
MAIL_FROM_NAME=AuditIT
```

---

## 🔧 Configuration Gmail

### Étape 1: Activer l'accès aux applications moins sécurisées

1. Allez sur [myaccount.google.com](https://myaccount.google.com)
2. Cliquez sur "Sécurité" dans le menu de gauche
3. Activez "Accès des applications moins sécurisées"

### Étape 2: Générer un mot de passe d'application

1. Allez sur [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Sélectionnez "Mail" et "Windows Computer"
3. Cliquez sur "Générer"
4. Copiez le mot de passe généré (16 caractères)

### Étape 3: Configuration

```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx  # Mot de passe d'application (sans espaces)
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_STARTTLS_REQUIRED=true
MAIL_FROM_NAME=AuditIT
```

---

## 🔧 Configuration Outlook/Office365

```env
SMTP_HOST=smtp.office365.com
SMTP_PORT=587
SMTP_USERNAME=your-email@outlook.com
SMTP_PASSWORD=your-password
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_STARTTLS_REQUIRED=true
MAIL_FROM_NAME=AuditIT
```

---

## 🔧 Configuration SendGrid

```env
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxx  # Votre clé API SendGrid
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_STARTTLS_REQUIRED=true
MAIL_FROM_NAME=AuditIT
```

---

## 📝 Utiliser le Service Email

### 1. Injecter le service

```java
@RestController
@RequiredArgsConstructor
public class MyController {
    private final EmailService emailService;
    
    @PostMapping("/test")
    public void sendEmail() {
        emailService.sendSimpleEmail(
            "user@example.com",
            "Sujet",
            "Corps du message"
        );
    }
}
```

### 2. Méthodes Disponibles

```java
// Email texte simple
emailService.sendSimpleEmail(to, subject, body);

// Email HTML
emailService.sendHtmlEmail(to, subject, htmlBody);

// Email à plusieurs destinataires
emailService.sendEmailToMultiple(recipients, subject, body);

// Email d'activation de compte
emailService.sendAccountActivationEmail(to, username, activationLink);

// Email de réinitialisation de mot de passe
emailService.sendPasswordResetEmail(to, username, resetLink);

// Notification générale
emailService.sendNotificationEmail(to, subject, message);
```

---

## 🧪 Tester l'Email

### Via les endpoints de test

**1. Email Simple**
```bash
GET http://localhost:8080/api/test/send-email?to=user@gmail.com&subject=Test&body=Ceci%20est%20un%20test
```

**2. Email HTML**
```bash
GET http://localhost:8080/api/test/send-html-email?to=user@gmail.com&subject=Test%20HTML
```

**3. Email d'Activation**
```bash
GET http://localhost:8080/api/test/send-activation-email?to=user@gmail.com&username=testuser
```

**4. Email de Réinitialisation**
```bash
GET http://localhost:8080/api/test/send-reset-password-email?to=user@gmail.com&username=testuser
```

---

## ❌ Troubleshooting

### Erreur: "Authentication failed"

**Cause:** Mauvais mot de passe ou identifiants
**Solution:**
- Vérifiez que `SMTP_USERNAME` et `SMTP_PASSWORD` sont corrects
- Pour Gmail, utilisez un mot de passe d'application (pas votre mot de passe normal)

### Erreur: "SMTP connection timeout"

**Cause:** Pare-feu ou configuration réseau
**Solution:**
- Vérifiez le port SMTP: 587 (TLS) ou 465 (SSL)
- Vérifiez que votre pare-feu permet la connexion sortante au serveur SMTP
- Augmentez les timeouts: `SMTP_CONNECTION_TIMEOUT=20000`

### Erreur: "STARTTLS not supported"

**Cause:** Serveur SMTP ne supporte pas STARTTLS
**Solution:**
- Désactivez STARTTLS si le serveur ne le supporte pas
- Ou utilisez un autre serveur SMTP

### Les emails ne sont pas reçus

**Causes potentielles:**
1. **Vérifiez le dossier Spam/Promotions**
2. **Les variables d'environnement ne sont pas chargées**
   ```bash
   # Créez un fichier .env
   SMTP_USERNAME=your-email@gmail.com
   SMTP_PASSWORD=your-app-password
   ```
3. **Le service n'est pas injecté correctement**
   - Vérifiez que `@RequiredArgsConstructor` est utilisé
4. **Pas d'erreur mais email pas reçu?**
   - Vérifiez les logs: `tail -f logs/spring.log`
   - Activez le mode debug: `SMTP_DEBUG=true`

---

## 🔐 Sécurité

### ⚠️ IMPORTANT

**NE JAMAIS** mettre les mots de passe dans le code!

```java
// ❌ MAUVAIS
spring.mail.password=my-secret-password

// ✅ BON
spring.mail.password=${SMTP_PASSWORD:}
```

### Utiliser les Variables d'Environnement

```bash
# Windows - Créer un fichier .env
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# Linux/Mac
export SMTP_USERNAME="your-email@gmail.com"
export SMTP_PASSWORD="your-app-password"
```

### Clés API Sécurisées

- Utilisez des mots de passe d'application (Gmail)
- Utilisez des clés API (SendGrid)
- Ne commitez JAMAIS les credentials à Git

---

## 📊 Logs

Pour déboguer les problèmes d'email:

```properties
# Dans application.properties
logging.level.com.pwc.auditit=DEBUG
logging.level.org.springframework.mail=DEBUG
spring.mail.properties.mail.debug=true
```

Puis vérifiez les logs:
```bash
tail -f logs/application.log | grep -i mail
```

---

## ✅ Checklist

- [ ] Variables d'environnement configurées
- [ ] `SMTP_USERNAME` et `SMTP_PASSWORD` corrects
- [ ] Port SMTP correct (587 pour TLS, 465 pour SSL)
- [ ] STARTTLS activé si supporté
- [ ] Tested via `/api/test/send-email`
- [ ] Email reçu dans inbox
- [ ] Vérifiez le dossier Spam
- [ ] Logs affichent "Email envoyé avec succès"

---

## 📚 Ressources

- [Spring Mail Documentation](https://spring.io/guides/gs/sending-email/)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [SendGrid SMTP Relay](https://docs.sendgrid.com/for-developers/sending-email/using-sendgrid-to-send-email)

---

**Besoin d'aide?** Vérifiez les logs et utilisez les endpoints de test `/api/test/*`


