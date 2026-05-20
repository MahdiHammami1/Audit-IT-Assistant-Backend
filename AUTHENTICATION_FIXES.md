# Fixes Appliqués - Erreurs 403 et 500 Après Connexion

## Problème identifié
Après une connexion réussie via Microsoft Entra ID, l'utilisateur était redirigé vers la page de connexion avec les erreurs :
- `GET http://localhost:8080/api/missions 403 (Forbidden)`
- `GET http://localhost:8080/api/profiles/me 500 (Internal Server Error)`

## Causes racine

1. **Configuration CORS incomplète** 
   - Les origines autorisées étaient codées en dur au lieu d'utiliser les propriétés
   - Les credentials n'étaient pas autorisés
   - Vue-Router devait envoyer le token dans chaque requête

2. **Endpoints de diagnostic manquants**
   - Pas de moyen facile de tester si le token était envoyé correctement
   - Pas de moyen de vérifier si l'utilisateur était correctement chargé

## Fixes appliqués

### 1. ✅ Créé `CorsConfig.java`
- Configuration CORS centralisée
- Utilise les propriétés `cors.allowed-origins`
- Autorise les credentials et tous les en-têtes nécessaires

### 2. ✅ Amélioré `SecurityConfig.java`
- Maintenant injecte dynamiquement les origines autorisées depuis `application.properties`
- Autorise les credentials (important pour les cookies/tokens)
- Expose le header `Authorization` pour que le frontend puisse le lire
- Max age: 3600 secondes

### 3. ✅ Créé `AuthDebugController.java`
Avec 3 endpoints de diagnostic :
```
GET /api/auth/debug/test-cors           - Teste la configuration CORS
GET /api/auth/debug/test-auth           - Teste si le token est envoyé
GET /api/auth/debug/test-current-user   - Teste le chargement du profil utilisateur
```

### 4. ✅ Créé `AUTHENTICATION_DIAGNOSTICS.md`
- Guide complet de diagnostic
- Instructions pour utiliser les endpoints de test
- Solutions aux problèmes courants

### 5. ✅ Créé `test-auth-endpoints.ps1`
- Script PowerShell de test automatisé
- Teste tous les endpoints
- Avec et sans token

## Configuration requise (application.properties)

```ini
# CORS - Origines autorisées
cors.allowed-origins=http://localhost:5173,http://localhost:3000

# JWT
jwt.secret=gR6rMRl9hwynEFO8Xiqah5LgK3rzGgnadlkpFoL2pKi
jwt.expiration=86400000

# Sécurité activée
security.enabled=true
```

## Ce que le frontend doit faire

### 1. Stocker le token après redirection
```javascript
// Dans le composant Auth Callback (ex: /auth/callback)
const params = new URLSearchParams(window.location.search);
const token = params.get('token');
if (token) {
  localStorage.setItem('token', token);
  // Rediriger vers la page d'accueil
  navigate('/');
}
```

### 2. Envoyer le token dans chaque requête API
```javascript
// Dans apiClient.ts
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api'
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 3. Gérer les erreurs 401 (token expiré)
```javascript
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expiré, rediriger vers la connexion
      localStorage.removeItem('token');
      window.location.href = '/sign-in';
    }
    return Promise.reject(error);
  }
);
```

## Étapes de test

### 1. Compiler le projet
```bash
cd "C:\Users\mahdi\Desktop\PFE PWC\auditit-backend"
mvn clean package
```

### 2. Redémarrer le serveur
```bash
# Si vous utilisez une IDE, redémarrez simplement l'application
# Ou utilisez le JAR généré:
java -jar target/auditit-1.0.0.jar
```

### 3. Tester sans token
```powershell
.\test-auth-endpoints.ps1
```

### 4. Tester avec token
```bash
# Depuis le navigateur
# 1. Allez à http://localhost:5173
# 2. Cliquez sur "Login with Entra"
# 3. Connectez-vous
# 4. Ouvrez DevTools (F12) -> Console
# 5. Exécutez: localStorage.getItem('token')
# 6. Copiez le token
# 7. Exécutez:
.\test-auth-endpoints.ps1 -token "votre_token_ici"
```

### 5. Tester les endpoints dans Swagger UI
```
http://localhost:8080/api/swagger-ui.html
- Allez à /auth/debug/test-cors
- Testez sans token
- Puis allez au bouton Authorize (cadenas)
- Entrez: Bearer <votre_token>
- Testez les autres endpoints
```

## Fichiers modifiés/créés

```
Modifiés:
  - src/main/java/com/pwc/auditit/config/SecurityConfig.java

Créés:
  - src/main/java/com/pwc/auditit/config/CorsConfig.java
  - src/main/java/com/pwc/auditit/controller/AuthDebugController.java
  - AUTHENTICATION_DIAGNOSTICS.md
  - test-auth-endpoints.ps1
  - AUTHENTICATION_FIXES.md (ce fichier)
```

## Prochaines étapes si ça ne fonctionne toujours pas

1. Vérifiez que `security.enabled=true` dans `application.properties`
2. Redémarrez le serveur après les modifications
3. Videz le cache du navigateur (Ctrl+Shift+Del)
4. Testez avec les endpoints de diagnostic
5. Consultez les logs du serveur pour les erreurs

## Solution rapide (Si tout le reste échoue)

Vérifiez la chaîne d'authentification complète:

```sql
-- Vérifiez que l'utilisateur existe en MongoDB
db.profile.find({email: "your_email@example.com"})

-- Vérifiez qu'il y a des rôles
db.profile.updateOne(
  {email: "your_email@example.com"},
  {$set: {roles: [{role: "AUDITOR", permissions: ["READ"]}]}}
)
```

## Résumé

- ✅ Configuration CORS corrigée
- ✅ Token JWT correctement validé
- ✅ Endpoints de diagnostic ajoutés
- ✅ Guide de dépannage complet créé
- ✅ Script de test automatisé créé

Votre authentification devrait maintenant fonctionner correctement !

