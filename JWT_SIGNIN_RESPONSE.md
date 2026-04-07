# ✅ JWT RETURNED ON SIGNIN - DOCUMENTATION

## 🎯 Endpoint SignIn Retourne le JWT

### Endpoint
```http
POST /api/auth/signin
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### ✅ Réponse Complète (Succès)
```json
{
  "success": true,
  "message": "Sign in successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE2NDY4MzIwMDAsImV4cCI6MTY0Njk5NDgwMH0.signature",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe"
    }
  },
  "timestamp": "2026-04-07T12:00:00.000Z"
}
```

---

## 📊 Structure de la Réponse

### ApiResponse<AuthResponse>
```json
{
  "success": boolean,           // ✅ true si succès
  "message": string,            // "Sign in successful"
  "data": {
    "token": string,            // ✅ JWT TOKEN
    "user": {
      "id": UUID,
      "email": string,
      "firstName": string,
      "lastName": string,
      "fullName": string
    }
  },
  "timestamp": Instant          // Quand la réponse a été envoyée
}
```

---

## 🔑 Utiliser le JWT Token Retourné

### 1️⃣ Extraire le token de la réponse
```typescript
// Frontend (TypeScript/React)
const response = await fetch('http://localhost:8080/api/auth/signin', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@example.com', password: 'password123' })
});

const { data } = await response.json();
const token = data.token;  // ✅ JWT Token
const user = data.user;    // Infos utilisateur
```

### 2️⃣ Stocker le token
```typescript
// Sauvegarder dans localStorage
localStorage.setItem('authToken', token);

// Ou sessionStorage
sessionStorage.setItem('authToken', token);
```

### 3️⃣ Envoyer le token dans les requêtes suivantes
```typescript
// Utiliser le token pour les requêtes protégées
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`
};

fetch('http://localhost:8080/api/profiles/me', {
  method: 'GET',
  headers: headers
});
```

---

## 📋 Exemple Complet

### Frontend - Login Flow
```typescript
// 1. Sign In
async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/auth/signin', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const result = await response.json();
  
  if (result.success) {
    // ✅ Récupérer le JWT
    const { token, user } = result.data;
    
    // Sauvegarder le token
    localStorage.setItem('authToken', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    console.log('✅ Login successful!');
    console.log('Token:', token);
    console.log('User:', user);
    
    // Rediriger vers le dashboard
    window.location.href = '/dashboard';
  } else {
    console.error('❌ Login failed:', result.message);
  }
}

// 2. Utiliser le token pour les requêtes authentifiées
async function getProfile() {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch('http://localhost:8080/api/profiles/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
}
```

---

## 🧪 Test avec curl

### Sign In
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Réponse
```json
{
  "success": true,
  "message": "Sign in successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe"
    }
  }
}
```

### Utiliser le token pour une requête protégée
```bash
TOKEN="eyJhbGciOiJIUzI1NiIs..."

curl -X GET http://localhost:8080/api/profiles/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔒 Points Importants

✅ **Le token est retourné dans la réponse SignIn**
✅ **Token situé dans: `response.data.token`**
✅ **Stocker le token dans localStorage ou sessionStorage**
✅ **Envoyer le token dans l'en-tête `Authorization: Bearer <token>`**
✅ **Le token expire après 24 heures** (configurable dans application.properties)

---

## 📊 Structure de Réponse de Succès vs Erreur

### ✅ Succès (201 Created)
```json
{
  "success": true,
  "message": "Sign in successful",
  "data": {
    "token": "...",
    "user": { ... }
  }
}
```

### ❌ Erreur (401 Unauthorized)
```json
{
  "success": false,
  "message": "Sign in failed: Invalid email or password",
  "data": null
}
```

---

## 🎯 Résumé

✅ **Le JWT est retourné après SignIn**
✅ **Localisation: `response.data.token`**
✅ **À utiliser dans l'en-tête `Authorization: Bearer {token}`**
✅ **Valide pour 24 heures**
✅ **Permet d'accéder aux ressources protégées**


