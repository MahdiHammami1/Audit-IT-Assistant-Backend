# 📦 ADVICE PACKAGE - GLOBAL EXCEPTION HANDLER

## ✅ Package Créé

**Chemin:** `com.pwc.auditit.advice`

**Fichier:** `GlobalExceptionHandler.java`

---

## 🎯 Fonctionnalités

### ✅ Exception Handlers Implémentés

| Exception | HTTP Status | Description |
|-----------|-----------|------------|
| **MethodArgumentNotValidException** | 400 | Erreurs de validation (champs) |
| **ResourceNotFoundException** | 404 | Ressource non trouvée |
| **AccessDeniedException** | 403 | Accès refusé |
| **MethodArgumentTypeMismatchException** | 400 | Type de paramètre invalide |
| **NoHandlerFoundException** | 404 | Endpoint inexistant |
| **IllegalArgumentException** | 400 | Argument invalide |
| **Exception** | 500 | Toute autre exception |
| **RuntimeException** | 500 | Exception runtime |

---

## 📊 Exemples de Réponses

### ✅ Erreur de Validation (400)
```json
{
  "success": false,
  "message": "",
  "data": {
    "email": "Email should be valid",
    "firstName": "First name is required"
  },
  "timestamp": "2026-04-07T12:00:00Z"
}
```

### ✅ Ressource Non Trouvée (404)
```json
{
  "success": false,
  "message": "Profile not found with id: 550e8400",
  "data": null,
  "timestamp": "2026-04-07T12:00:00Z"
}
```

### ✅ Accès Refusé (403)
```json
{
  "success": false,
  "message": "Access denied: User is not authorized",
  "data": null,
  "timestamp": "2026-04-07T12:00:00Z"
}
```

### ✅ Erreur Serveur (500)
```json
{
  "success": false,
  "message": "Internal server error: Connection timeout",
  "data": null,
  "timestamp": "2026-04-07T12:00:00Z"
}
```

---

## 🔍 Logging

Tous les handlers incluent du logging approprié :

- ⚠️ **WARN** - Pour les erreurs métier (404, 403, validation)
- ❌ **ERROR** - Pour les erreurs serveur (500)

```
2026-04-07 12:00:00 [main] WARN  c.p.a.advice.GlobalExceptionHandler - Resource not found: Profile not found
2026-04-07 12:00:00 [main] ERROR c.p.a.advice.GlobalExceptionHandler - Unexpected error occurred:
```

---

## ⚙️ Configuration Recommandée

Pour activer la détection des endpoints non trouvés, ajoutez à `application.properties` :

```properties
server.error.include-message=always
server.error.include-binding-errors=always
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
```

---

## 🚀 Avantages

✅ **Centralisé** - Tous les traitements d'exception au même endroit
✅ **Cohérent** - Réponses au format uniforme (ApiResponse)
✅ **Détaillé** - Messages d'erreur clairs et informatifs
✅ **Sécurisé** - Logging des erreurs pour debugging
✅ **Maintenable** - Facile à ajouter de nouveaux handlers

---

## 📝 Ajouter de Nouveaux Handlers

Exemple : Ajouter un handler pour `CustomException`

```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<ApiResponse<Void>> handleCustomException(
        CustomException ex) {
    log.warn("Custom error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
}
```

---

## ✅ Compilation

```
✅ BUILD SUCCESS
✅ GlobalExceptionHandler compilé
✅ Aucune dépendance supplémentaire
```

---

## 🎯 Utilisation

Les controllers **n'ont rien à faire**. Le GlobalExceptionHandler s'active **automatiquement** :

```java
@RestController
@RequestMapping("/profiles")
public class ProfileController {
    // N'importe quelle exception levée sera capturée
    // et gérée par GlobalExceptionHandler
}
```

---

## ✨ Résumé

✅ Package `advice` créé
✅ `GlobalExceptionHandler` implémenté
✅ 8 types d'exceptions gérées
✅ Réponses uniformes en format `ApiResponse`
✅ Logging détaillé
✅ Prêt à l'emploi

**Le gestionnaire d'exceptions global fonctionne maintenant automatiquement pour tous les controllers ! 🎉**


