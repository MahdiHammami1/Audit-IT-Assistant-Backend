# ⚡ Prochaines Étapes - Guide d'Action

## ✅ Ce qui a été fait

1. ✅ Configuration CORS dynamique créée
2. ✅ SecurityConfig amélioré
3. ✅ Endpoints de diagnostic ajoutés
4. ✅ Le projet compile sans erreurs
5. ✅ Documentation complète créée

## 🚀 À faire maintenant

### Phase 1 : Redémarrer le serveur
```powershell
# Option 1 : Si vous utilisez une IDE (VS Code, IntelliJ)
# - Redémarrez simplement l'application

# Option 2 : Depuis PowerShell
cd "C:\Users\mahdi\Desktop\PFE PWC\auditit-backend"
java -jar target/auditit-1.0.0.jar
```

**Attendez le message Tomcat :**
```
TomcatWebServer - Tomcat started on port 8080 (http) with context path '/api'
```

---

### Phase 2 : Tester les endpoints de diagnostic

#### Test 1 : Vérifier CORS (pas de token requis)
```powershell
# Ligne de commande PowerShell
curl -Method GET "http://localhost:8080/api/auth/debug/test-cors"

# Réponse attendue
{
  "success": true,
  "message": "CORS is working",
  "data": null
}
```

#### Test 2 : Test sans token
```powershell
.\test-auth-endpoints.ps1
```

**Vous devriez voir :**
```
✅ Test: GET /auth/debug/test-cors
✅ Status: 200
Response:
  success: true
  message: CORS is working

✅ Test: GET /auth/debug/test-auth
✅ Status: 200
Response:
  authHeaderPresent: false
  securityContextAuth: false
```

---

### Phase 3 : Tester avec votre token

#### 3.1 Obtenir un token auprès du navigateur
1. Allez à : http://localhost:5173
2. Cliquez sur "Login with Entra"
3. Connectez-vous avec votre compte
4. Vous serez redirigé vers : http://localhost:5173/auth/callback?token=eyJ...
5. Ouvrez DevTools (F12) → Console
6. Exécutez : `localStorage.getItem('token')`
7. Copiez le token (environ 200 caractères)

#### 3.2 Tester avec le token
```powershell
# Remplacez YOUR_TOKEN par votre token réel
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiMmRiMWVmOS1mYjI0LTQzOTYtOTU5YS0wMzk5NmIwZDg5NTAiLCJlbWFpbCI6Im1haGRpLmhhbW1hbWlAaXBzaS51LW1hbm91YmEudG4iLCJpYXQiOjE3NzgwNjcwOTgsImV4cCI6MTc3ODE1MzQ5OH0.wGsI036jteJl6kGsD3UaH9K8XgNg4W08TvcUbaGM0j8"

# Exécutez le script de test
.\test-auth-endpoints.ps1 -token $token
```

**Vous devriez voir :**
```
✅ Test: GET /auth/debug/test-auth
✅ Status: 200
Response:
  authHeaderPresent: true
  authHeaderFormat: Bearer token
  tokenLength: 200+
  securityContextAuth: true
  authenticationPrincipal: Profile
  isAuthenticated: true

✅ Test: GET /auth/debug/test-current-user
✅ Status: 200
Response:
  status: success
  userId: b2db1ef9-fb24-4396-959a-03996b0d8950
  email: mahdi.hammami@ipsi.u-manouba.tn
  fullName: mahdi.hammami
```

---

### Phase 4 : Tester les endpoints réels

```powershell
# Avec le même token
$token = "YOUR_TOKEN_HERE"

# Test missions
curl -Method GET `
  -Headers @{"Authorization"="Bearer $token"} `
  "http://localhost:8080/api/missions"

# Test profil
curl -Method GET `
  -Headers @{"Authorization"="Bearer $token"} `
  "http://localhost:8080/api/profiles/me"
```

**Réponses attendues :**
```json
// GET /missions
{
  "success": true,
  "data": [
    {
      "id": "...",
      "name": "...",
      "status": "..."
    }
  ]
}

// GET /profiles/me
{
  "success": true,
  "data": {
    "id": "b2db1ef9-fb24-4396-959a-03996b0d8950",
    "email": "mahdi.hammami@ipsi.u-manouba.tn",
    "fullName": "mahdi.hammami"
  }
}
```

---

### Phase 5 : Tester via Swagger UI

1. Allez à : http://localhost:8080/api/swagger-ui.html
2. Cherchez : "Authentication Debug"
3. Testez les endpoints (sans token en premier)
4. Ensuite, cliquez sur le bouton "Authorize" (cadenas) en haut à droite
5. Copiez votre token et mettez : `Bearer <votre_token>`
6. Cliquez "Authorize"
7. Testez les endpoints et missions

---

## 🐛 Dépannage par symptôme

### Symptôme 1 : "CORS Error"
```
Access to XMLHttpRequest at 'http://localhost:8080/api/missions' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**Solutions:**
1. Vérifiez que `cors.allowed-origins` contient `http://localhost:5173`
2. Vérifiez que le serveur est redémarré
3. Videz le cache du navigateur : Ctrl+Shift+Del
4. Testez avec `/auth/debug/test-cors`

---

### Symptôme 2 : "403 Forbidden"
```
GET http://localhost:8080/api/missions 403 (Forbidden)
```

**Solutions:**
1. Vérifiez que le token est envoyé : `.\test-auth-endpoints.ps1 -token $token`
2. Si `authHeaderPresent: false` → le frontend n'envoie pas le token
   - Vérifiez votre apiClient.ts
   - Vérifiez que localStorage a le token
3. Si `authHeaderPresent: true` mais 403 → Aucune permission
   - L'utilisateur doit avoir des rôles assignés
   - Vérifiez dans MongoDB

---

### Symptôme 3 : "500 Internal Server Error"
```
GET http://localhost:8080/api/profiles/me 500 (Internal Server Error)
```

**Solutions:**
1. Vérifiez les logs du serveur
2. Vérifiez que l'utilisateur existe en MongoDB :
   ```javascript
   db.profile.findOne({email: "your_email@example.com"})
   ```
3. Testez avec `/auth/debug/test-current-user`
4. Vérifiez que le token est valide

---

### Symptôme 4 : "401 Unauthorized"
```
GET http://localhost:8080/api/profiles/me 401 (Unauthorized)
```

**Cause:** Token expiré

**Solution:**
1. Reconnectez-vous via Entra ID
2. Un nouveau token sera généré

---

## 📊 Checklist de Vérification

### Serveur Backend
- [ ] Le serveur Java est démarré
- [ ] Tomcat tourne sur le port 8080
- [ ] Le contexte est `/api`
- [ ] Les logs n'affichent pas d'erreurs critiques

### Configuration
- [ ] `security.enabled=true` dans application.properties
- [ ] `cors.allowed-origins` contient votre frontend URL
- [ ] `jwt.secret` et `jwt.expiration` sont configurés
- [ ] `auth.entra.*` properties sont configurées

### Frontend
- [ ] Le token est reçu après redirection Entra ID
- [ ] Le token est stocké en localStorage
- [ ] Le token est envoyé dans le header `Authorization: Bearer`
- [ ] Le token n'est pas expiré

### Endpoints
- [ ] `/auth/debug/test-cors` → 200 OK
- [ ] `/auth/debug/test-auth` (avec token) → authHeaderPresent: true
- [ ] `/auth/debug/test-current-user` (avec token) → 200 OK avec profil
- [ ] `/missions` (avec token) → 200 OK avec missions
- [ ] `/profiles/me` (avec token) → 200 OK avec profil

---

## 📞 Support

Si vous avez toujours des problèmes après avoir suivi ces étapes :

1. Consultez les fichiers de documentation :
   - `AUTHENTICATION_DIAGNOSTICS.md`
   - `AUTHENTICATION_FIXES.md`
   - `SOLUTION_SUMMARY_AUTH.md`

2. Vérifiez les logs :
   - `app.log`
   - Console de l'IDE
   - Logs du navigateur (F12)

3. Testez avec les scripts fournis :
   - `test-auth-endpoints.ps1`
   - Swagger UI

4. Recherchez les patterns d'erreur courants :
   - "JWT validation failed"
   - "Profile not found"
   - "CORS policy"
   - "401 Unauthorized"

---

## ✨ Résumé

1. **Redémarrer le serveur**
2. **Tester CORS** : `.\test-auth-endpoints.ps1`
3. **Obtenir un token** : Connectez-vous via Entra ID
4. **Tester avec token** : `.\test-auth-endpoints.ps1 -token $token`
5. **Vérifier le frontend** : Stocke et envoie bien le token
6. **Tester les endpoints réels** : /missions, /profiles/me

---

🎉 **Vous êtes prêt à tester l'authentification complète !**

