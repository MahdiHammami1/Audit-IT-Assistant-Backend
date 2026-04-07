# 📊 SWAGGER - Tous les Endpoints Profiles

## ✅ GET Endpoints

### GET /profiles
**Lister tous les profils**
- ✅ Accès: Public
- Réponse: `ApiResponse<List<ProfileResponse>>`

### GET /profiles/me
**Récupérer le profil courant**
- ✅ Accès: Authentifié
- Réponse: `ApiResponse<ProfileResponse>`

### GET /profiles/{id}
**Récupérer un profil par ID**
- ✅ Accès: Public
- Paramètre: `id` (UUID)
- Réponse: `ApiResponse<ProfileResponse>`

---

## ✅ POST Endpoints

*(Aucun POST pour les profiles)*

---

## ✅ PUT Endpoints

### PUT /profiles/{id}
**Mise à jour complète d'un profil**
- 🔒 Accès: ADMIN ou Owner
- Paramètre: `id` (UUID)
- Body: `UpdateProfileRequest`
  ```json
  {
    "email": "newemail@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
  ```
- Réponse: `ApiResponse<ProfileResponse>`

### PUT /profiles/me
**Mise à jour du profil courant**
- 🔒 Accès: Authentifié (User)
- Body: `UpdateProfileRequest`
- Réponse: `ApiResponse<ProfileResponse>`

---

## ✅ PATCH Endpoints

### PATCH /profiles/{id}
**Mise à jour partielle d'un profil**
- 🔒 Accès: ADMIN ou Owner
- Paramètre: `id` (UUID)
- Body: `UpdateProfileRequest` (seulement les champs à mettre à jour)
  ```json
  {
    "firstName": "Jean"
  }
  ```
- Réponse: `ApiResponse<ProfileResponse>`

---

## ✅ DELETE Endpoints

### DELETE /profiles/{id}
**Supprimer un profil**
- 🔒 Accès: ADMIN
- Paramètre: `id` (UUID)
- Réponse: `ApiResponse<Void>`

### DELETE /profiles/batch
**Supprimer plusieurs profils**
- 🔒 Accès: ADMIN
- Body: `List<UUID>`
  ```json
  [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440001"
  ]
  ```
- Réponse: `ApiResponse<Void>`

---

## 📋 DTO: UpdateProfileRequest

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;
    
    @Size(max = 500)
    private String avatarUrl;
}
```

---

## 📋 DTO: ProfileResponse

```java
@Data
@Builder
public class ProfileResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String initials;
    private String avatarUrl;
    private Set<String> roles;
}
```

---

## 🔐 Règles de Sécurité

| Endpoint | Role ADMIN | Owner | Public |
|----------|-----------|-------|--------|
| GET /profiles | ✅ | ✅ | ✅ |
| GET /profiles/me | ✅ | ✅ | - |
| GET /profiles/{id} | ✅ | ✅ | ✅ |
| PUT /profiles/{id} | ✅ | ✅ | - |
| PATCH /profiles/{id} | ✅ | ✅ | - |
| PUT /profiles/me | ✅ | ✅ | - |
| DELETE /profiles/{id} | ✅ | - | - |
| DELETE /profiles/batch | ✅ | - | - |

---

## 🚀 Accès à Swagger UI

```
http://localhost:8080/api/swagger-ui.html
```

Vous pouvez tester tous les endpoints directement depuis l'interface Swagger UI.

---

## ✨ Résumé

**7 Endpoints pour gérer complètement les profils:**
- ✅ 3 GET (lire)
- ✅ 2 PUT (mettre à jour)
- ✅ 1 PATCH (mettre à jour partiellement)
- ✅ 2 DELETE (supprimer)

**CRUD complet et sécurisé !**


