# Guide de Diagnostic - Erreurs 403 et 500 Après Connexion

## Résumé des erreurs
1. **403 Forbidden** sur `GET /api/missions`
2. **500 Internal Server Error** sur `GET /api/profiles/me`

## Causes possibles

### Erreur 403 (Forbidden)
- ✅ Le token JWT est envoyé
- ❌ L'utilisateur n'a pas les permissions requises
- **Solution**: Assurez-vous que l'utilisateur créé via Entra ID a les rôles appropriés

### Erreur 500 (Internal Server Error)
- ❌ Le token n'est pas envoyé OU
- ❌ Le token est invalide/expiré OU
- ❌ Il y a une exception serveur non capturée
- **Solution**: Utiliser les endpoints de diagnostic

## Points de vérification

### 1. Vérifier que le token est stocké et envoyé correctement

#### A. Vérifier le stockage du token (Frontend)
Après la redirection de `/auth/callback?token=xxxxx`, vérifiez que :
```javascript
// Dans votre composant Auth callback
const params = new URLSearchParams(window.location.search);
const token = params.get('token');
console.log('Token from URL:', token);

// Stocker le token
localStorage.setItem('token', token);
console.log('Token stocké:', localStorage.getItem('token'));
```

#### B. Vérifier l'envoi du token (Frontend - API Client)
Assurez-vous que chaque requête inclut le header:
```javascript
// Dans votre apiClient.ts
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('token')}`,
  'Content-Type': 'application/json'
};
// Ajouter ces headers à chaque appel API
```

### 2. Tester les endpoints de diagnostic

#### Test 1: Vérifier que CORS fonctionne
```
GET http://localhost:8080/api/auth/debug/test-cors
(Sans token requis)
```

**Réponse attendue:**
```json
{
  "success": true,
  "message": "CORS is working"
}
```

#### Test 2: Vérifier que le token est envoyé
```
GET http://localhost:8080/api/auth/debug/test-auth
Header: Authorization: Bearer <votre_token>
```

**Réponse attendue si token est envoyé:**
```json
{
  "success": true,
  "data": {
    "authHeaderPresent": true,
    "authHeaderFormat": "Bearer token",
    "tokenLength": 150,
    "securityContextAuth": true,
    "authenticationPrincipal": "Profile",
    "isAuthenticated": true
  }
}
```

**Si le token n'est pas envoyé, vous verrez:**
```json
{
  "success": true,
  "data": {
    "authHeaderPresent": false,
    "securityContextAuth": false
  }
}
```

#### Test 3: Vérifier que l'utilisateur est correctement chargé
```
GET http://localhost:8080/api/auth/debug/test-current-user
Header: Authorization: Bearer <votre_token>
```

**Réponse attendue:**
```json
{
  "success": true,
  "data": {
    "status": "success",
    "userId": "xxx-xxx-xxx",
    "email": "user@example.com",
    "fullName": "User Full Name"
  }
}
```

### 3. Utiliser Swagger UI pour tester
1. Allez à: http://localhost:8080/api/swagger-ui.html
2. Cherchez l'endpoint que vous voulez tester
3. Cliquez sur "Try it out"
4. Pour les endpoints nécessitant auth:
   - Allez à "/auth/debug/test-cors" (pas besoin de token)
   - Copiez le token du paramètre d'URL de redirection
   - Dans Swagger, cliquez sur le bouton "Authorize" (petit cadenas)
   - Entrez: `Bearer <votre_token>`
   - Testez ensuite les autres endpoints

### 4. Vérifier les logs du serveur
Consultez les logs pour les erreurs:
```
app.log ou build.log
```

Cherchez les patterns:
- `JWT validation failed`
- `Profile not found`
- `NullPointerException`
- `AuthenticationException`

## Causes courantes et solutions

### Problème: Token n'est pas envoyé
**Symptôme**: `authHeaderPresent: false` dans le test
**Solution**:
1. Vérifiez que le frontend stocke le token correctement
2. Vérifiez que le header `Authorization: Bearer` est ajouté à chaque requête
3. Vérifiez les paramètres CORS (actuellement: `http://localhost:5173`)

### Problème: Token expiré
**Symptôme**: `isTokenValid: false` ou `401 Unauthorized`
**Solution**:
1. Re-faire la connexion pour obtenir un nouveau token
2. Le token expire après 24h (86400000 ms)

### Problème: Utilisateur n'existe pas en base de données
**Symptôme**: `securityContextAuth: true` mais `500 Internal Server Error`
**Solution**:
1. Vérifier que l'utilisateur a été créé lors de la connexion Entra ID
2. Vérifier la base de données MongoDB pour voir s'il y a un profil avec cet email
3. Récréer l'utilisateur si nécessaire

### Problème: Rôles manquants (403 Forbidden)
**Symptôme**: `isAuthenticated: true` mais `403 Forbidden` sur /missions
**Solution**:
1. L'utilisateur doit avoir au moins un rôle assigné
2. Vérifier dans MongoDB:
   ```
   db.profile.findOne({email: "user@example.com"})
   // Regarder le champ "roles"
   ```
3. Assigner des rôles via l'endpoint `/api/profiles/{id}/roles`

## Architecture du flux d'authentification

```
1. Frontend: Utilisateur clique "Login with Entra"
   ↓
2. Frontend: Redirige vers http://localhost:8080/api/auth/entra/login
   ↓
3. Backend: Retourne l'URL Microsoft Entra ID
   ↓
4. Frontend: Redirige l'utilisateur vers Microsoft
   ↓
5. Microsoft: L'utilisateur se connecte et autorise l'app
   ↓
6. Microsoft: Redirige vers http://localhost:8080/api/auth/callback?code=xxx
   ↓
7. Backend: 
   - Échange le code pour un token Entra ID
   - Récupère les infos utilisateur de Microsoft
   - Crée/Met à jour le profil utilisateur en BD
   - Génère un JWT token
   - Redirige vers http://localhost:5173/auth/callback?token=JWT
   ↓
8. Frontend: 
   - Extrait le token de l'URL
   - Stocke le token en localStorage
   - Envoie le token dans l'header "Authorization: Bearer" pour chaque requête
   ↓
9. Backend:
   - Valide le JWT
   - Charge le profil utilisateur depuis la BD
   - Exécute l'endpoint
```

## Fichiers concernés

- `SecurityConfig.java` - Configuration CORS et JWT
- `JwtAuthenticationFilter.java` - Validation du JWT
- `JwtService.java` - Génération et validation du token
- `AuthEntraController.java` - Callback Entra ID
- `AuthDebugController.java` - Endpoints de test
- `application.properties` - Configuration (cors.allowed-origins)

## Prochaines étapes

1. Testez les endpoints de diagnostic (test-cors, test-auth, test-current-user)
2. Vérifiez les logs du serveur
3. Vérifiez les requêtes dans le navigateur (F12 -> Network -> Headers)
4. Signalez les résultats des tests de diagnostic pour un diagnostic plus précis

