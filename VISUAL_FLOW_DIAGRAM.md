# 📊 Flux Visuel du Sign-Up Automatique

## Avant vs Après

### AVANT (Erreurs 403 & 500)
```
UTILISATEUR
    │
    ▼
LOGIN AVEC MICROSOFT
    │
    ▼
BACKEND CRÉE USER SANS RÔLE
    │
    ├─ Profil : ✓ Créé
    └─ Rôle   : ✗ MANQUANT ❌
    │
    ├─ /profiles/me → 500 ERROR ❌
    └─ /missions    → 403 FORBIDDEN ❌
```

### APRÈS (Fonctionnement normal)
```
UTILISATEUR
    │
    ▼
LOGIN AVEC MICROSOFT
    │
    ▼
BACKEND CRÉE USER AVEC RÔLE
    │
    ├─ Profil  : ✓ Créé
    ├─ Rôle    : ✓ AUDITOR (assigné)
    └─ Verified: ✓ TRUE
    │
    ├─ /profiles/me → 200 OK ✅
    ├─ /missions    → 200 OK ✅
    └─ Page accueil → AFFICHÉE ✅
```

---

## Détail du Flux Complet

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. UTILISATEUR SE CONNECTE AVEC MICROSOFT                       │
└─────────────────────────────────────────────────────────────────┘
         │
         │ Allez à: http://localhost:5173/login
         │ Cliquez: "Login with Microsoft"
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. REDIRECTION VERS MICROSOFT ENTRA ID                          │
│    (URL: login.microsoftonline.com/common/oauth2/authorize)    │
└─────────────────────────────────────────────────────────────────┘
         │
         │ Utilisateur se connecte + autorise l'app
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. MICROSOFT REDIRIGE VERS BACKEND                              │
│    (GET /api/auth/callback?code=ABC123&state=XYZ)             │
└─────────────────────────────────────────────────────────────────┘
         │
         │ Code: ABC123 (authorization code)
         │ State: XYZ (CSRF protection)
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. BACKEND ÉCHANGE CODE POUR TOKEN                              │
│    EntraIdAuthService.exchangeCodeForToken(code)               │
└─────────────────────────────────────────────────────────────────┘
         │
         │ HTTP Request au token endpoint de Microsoft
         │ Paramètres: client_id, client_secret, code, redirect_uri
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. EXTRACTING USER INFO FROM ID TOKEN                           │
│    EntraIdAuthService.extractUserInfoFromIdToken()             │
└─────────────────────────────────────────────────────────────────┘
         │
         │ Décode le JWT
         │ Extraie: email, givenName, familyName, displayName
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. TROUVE OU CRÉE L'UTILISATEUR EN BD                           │
│    EntraIdAuthService.findOrCreateProfile(userInfo)            │
└─────────────────────────────────────────────────────────────────┘
         │
         │
         ├─────────────────────┬──────────────────────┐
         │                     │                      │
         ▼                     ▼                      ▼
    L'UTILISATEUR    L'UTILISATEUR      (PAS POSSIBLE)
    EXISTE DÉJÀ      N'EXISTE PAS
    (SIGN-IN)        (SIGN-UP) ← NOUVEAU!
         │                     │
         │                     ▼
         │            ┌──────────────────────────┐
         │            │ 1. CREATE PROFILE        │
         │            │    ├─ id: UUID           │
         │            │    ├─ email              │
         │            │    ├─ firstName          │
         │            │    ├─ lastName           │
         │            │    ├─ fullName           │
         │            │    ├─ password (random)  │
         │            │    └─ verified: true     │
         │            └──────────────────────────┘
         │                     │
         │                     ▼
         │            ┌──────────────────────────┐
         │            │ 2. ASSIGN ROLE (NOUVEAU)│
         │            │    ├─ id: UUID           │
         │            │    ├─ user: Profile ID   │
         │            │    └─ role: "auditor"    │
         │            └──────────────────────────┘
         │                     │
         │                     ▼
         │            MongoDB (collections mises à jour)
         │                     │
         ├─────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7. GÉNÉRER JWT TOKEN LOCAL                                      │
│    JwtService.generateToken(userId, email)                     │
└─────────────────────────────────────────────────────────────────┘
         │
         │ JWT = Header.Payload.Signature
         │ Expiration: 24 heures
         │ Payload: { sub: userId, email: email }
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 8. REDIRECTION VERS FRONTEND AVEC TOKEN                         │
│    HTTP 302 Found                                               │
│    Location: http://localhost:5173/auth/callback?token=JWT    │
└─────────────────────────────────────────────────────────────────┘
         │
         │ Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 9. FRONTEND TRAITE LE CALLBACK                                  │
│    /auth/callback?token=JWT                                    │
└─────────────────────────────────────────────────────────────────┘
         │
         │ ├─ Extrait token de l'URL
         │ ├─ Stocke en localStorage
         │ ├─ Configure axios/fetch header
         │ └─ Redirige vers page d'accueil
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 10. UTILISATEUR AUTHENTIFIÉ ET AUTORISÉ                         │
│     NAVIGATION DANS L'APP                                       │
└─────────────────────────────────────────────────────────────────┘
         │
         │ ✅ GET /api/missions        → 200 OK
         │ ✅ GET /api/profiles/me     → 200 OK
         │ ✅ Page d'accueil affichée  → OK
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ FIN : AUTHENTIFICATION COMPLÈTE                                 │
│ - User créé en BD                                               │
│ - Rôle assigné                                                  │
│ - JWT token valide (24h)                                        │
│ - Prêt à utiliser l'app                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Structure des Données

### Avant (SANS rôle)
```
MongoDB collections:
┌──────────────────┐
│    profiles      │
├──────────────────┤
│ _id: UUID        │  ← Créé lors du sign-up
│ email: xxx@xx    │
│ firstName: John  │
│ lastName: Doe    │
│ fullName: ...    │
│ verified: true   │
└──────────────────┘

┌──────────────────┐
│   user_roles     │
├──────────────────┤
│ (VIDE)           │  ← PROBLÈME! Pas de rôle
│                  │
└──────────────────┘

RÉSULTATS:
❌ /profiles/me   → 500 ERROR
❌ /missions      → 403 FORBIDDEN
```

### Après (AVEC rôle)
```
MongoDB collections:
┌──────────────────┐
│    profiles      │
├──────────────────┤
│ _id: UUID        │  ← Créé lors du sign-up
│ email: xxx@xx    │
│ firstName: John  │
│ lastName: Doe    │
│ fullName: ...    │
│ verified: true   │
└──────────────────┘

┌──────────────────┐
│   user_roles     │
├──────────────────┤
│ _id: UUID        │  ← NOUVEAU! Rôle créé
│ user: UUID       │  ← Référence au profil
│ role: "auditor"  │  ← Rôle assigné
└──────────────────┘

RÉSULTATS:
✅ /profiles/me   → 200 OK (Profil trouvé)
✅ /missions      → 200 OK (Rôle trouvé)
✅ Page d'accueil → AFFICHÉE
```

---

## Comparaison Timeline

### Avant
```
T=0   │ Login Microsoft
T=1   │ Code reçu
T=2   │ Token exchangé
T=3   │ User créé
T=4   │ STOP! Pas de rôle → 403 ❌
      │ Erreur 500 ❌
      │ === FIN DISASTER ===
```

### Après
```
T=0   │ Login Microsoft
T=1   │ Code reçu
T=2   │ Token exchanged
T=3   │ User créé
T=4   │ Rôle 'auditor' assigné ✅
T=5   │ JWT généré ✅
T=6   │ Redirigé avec token ✅
T=7   │ Frontend reçoit token ✅
T=8   │ Page d'accueil ✅
      │ === SUCCESS! ===
```

---

## Matrice de Requêtes API

### Avant Sign-Up Fix
```
┌────────────────────────────────────────────┐
│  API REQUEST               │  BEFORE      │
├──────────────────────────────────────────┤
│ GET /profiles/me           │  500 ERROR   │
│ GET /missions              │  403 FORBIDDEN│
│ GET /missions/{id}         │  403 FORBIDDEN│
│ POST /missions             │  403 FORBIDDEN│
│ GET /profiles              │  ✅ OK       │
│ GET /auth/debug/test-cors  │  ✅ OK       │
└────────────────────────────────────────────┘
```

### Après Sign-Up Fix
```
┌────────────────────────────────────────────┐
│  API REQUEST               │  AFTER       │
├──────────────────────────────────────────┤
│ GET /profiles/me           │  ✅ OK       │
│ GET /missions              │  ✅ OK       │
│ GET /missions/{id}         │  ✅ OK       │
│ POST /missions             │  ✅ OK       │
│ GET /profiles              │  ✅ OK       │
│ GET /auth/debug/test-cors  │  ✅ OK       │
└────────────────────────────────────────────┘
```

---

## États de l'Utilisateur

### Phase 1: Pas connecté
```
Status: Anonymous
JWT Token: NONE
Profile: NONE
Role: NONE
```

### Phase 2: Pendant la connexion
```
Status: Authenticating
Location: Microsoft login
JWT Token: NONE
Profile: Being created
Role: Being assigned
```

### Phase 3: Après la connexion
```
Status: Authenticated
JWT Token: eyJ...
Profile: CREATED ✅
Role: auditor ✅
Verified: true ✅
```

---

## Résumé Visuel des Changements

```
                    AVANT                        APRÈS
                    ════════                     ═════

OpenID Token   ────────────────────┐      ────────────────────┐
               │                   │      │                   │
               ▼                   │      ▼                   │
         Profile Created ──────────┤ Profile Created ────────┤
         (Verified = True)         │ (Verified = True) ✅    │
               │                   │      │                   │
               ▼                   │      ▼                   │
         NO ROLE ❌ ◄──────────────┤ Role = "auditor" ✅ ◄──┤
               │                   │      │                   │
               ▼                   │      ▼                   │
    Error: 403 / 500 ❌            │ JWT Generated ✅
               │                   │      │
               ▼                   │      ▼
    Page d'accueil NOK             │ Page d'accueil OK ✅
```

---

## Impact sur les Collections MongoDB

```
PROFILES Collection Evolution
═══════════════════════════════

BEFORE Sign-Up Fix:
  1 user connecté → 1 document profile
  (Mais sans rôle associé)

AFTER Sign-Up Fix:
  1 user connecté → 1 document profile
                  → 1 document user_role (NEW!)

MongoDB Records:
  Profiles: 1 new user
  User Roles: 1 new role association ← NEW!
```

---

## Logs Produits

### Avant
```
[INFO] Entra ID authentication completed successfully for user: mahdi@example.com
```

### Après
```
[INFO] Creating new profile for Entra ID user (Sign-up flow): mahdi@example.com
[INFO] New user profile created successfully for Entra ID user: mahdi@example.com (ID: xxx-xxx)
[INFO] Default role 'auditor' assigned to new user: mahdi@example.com
[INFO] Authentication completed successfully for user: mahdi@example.com
```

---

## Bénéfices Visuels

```
CÔTÉ UTILISATEUR                CÔTÉ BACKEND
═════════════════               ════════════

Avant:                          Avant:
─────                           ─────
1. Login ✓                      1. Code exchange ✓
2. Authorization ✓             2. Token extraction ✓
3. Microsoft ✓                 3. User creation ✓
4. Redirect ✓                  4. NO ROLE ✗ BUG!
5. ERROR 500 ✗                 5. 403 Forbidden ✗
6. ERROR 403 ✗
7. See login page AGAIN ✗      

User: 😞 FRUSTRATED


Après:                          Après:
──────                          ──────
1. Login ✓                      1. Code exchange ✓
2. Authorization ✓             2. Token extraction ✓
3. Microsoft ✓                 3. User creation ✓
4. Redirect ✓                  4. ROLE ASSIGNED ✓ FIX!
5. See home page ✓             5. JWT generated ✓
6. Access /missions ✓          6. 200 OK ✓
7. Access /profiles/me ✓

User: 😊 HAPPY
```

---

## Résumé Ultra-Rapide

```
┌─ UTILISATEUR MICROSOFT ─┐
│                         │
│ Se connecte             │
│      ↓                  │
│ ✅ Sign-Up Auto!        │
│      ↓                  │
│ ✅ Profil créé          │
│      ↓                  │
│ ✅ Rôle "auditor"       │
│      ↓                  │
│ ✅ JWT généré           │
│      ↓                  │
│ ✅ Page accueil OK      │
│                         │
└─────────────────────────┘
```

---

**La beauté du changement en une image** :

```
AVANT:  403 ❌  500 ❌
APRÈS:  200 ✅  200 ✅
```

