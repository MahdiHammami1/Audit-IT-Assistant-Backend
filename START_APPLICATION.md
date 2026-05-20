# 🚀 Démarrer l'application AuditIT

## 📋 Prérequis vérifiés
✅ Application compilée avec succès  
✅ JAR généré: `target/auditit-1.0.0.jar`  
✅ Aucune erreur de compilation  

---

## 🎯 Option 1: Via Maven (Recommandé pour développement)

```bash
cd "C:\Users\mahdi\Desktop\PFE PWC\auditit-backend"
mvn spring-boot:run
```

**Avantages**:
- Rechargement automatique des fichiers
- Logs en temps réel
- Facile à arrêter avec Ctrl+C

**Résultat attendu**:
```
Started AuditItApplication in X.XXX seconds
Listening on: http://localhost:8080
```

---

## 🎯 Option 2: Lancer le JAR directement

```bash
cd "C:\Users\mahdi\Desktop\PFE PWC\auditit-backend"
java -jar target/auditit-1.0.0.jar
```

**Avantages**:
- Simulé la production
- Plus rapide au démarrage
- Contrôle total de JVM

**Options JVM avancées** (optionnel):
```bash
java -Xmx2G -Xms1G -jar target/auditit-1.0.0.jar
```

---

## 🎯 Option 3: Via IDE JetBrains IntelliJ

1. Ouvrir le projet dans IntelliJ
2. Localiser: `src/main/java/com/pwc/auditit/AuditItApplication.java`
3. Clic droit → Run 'AuditItApplication'
4. **OU** Utiliser le menu: Run → Run 'AuditItApplication'

**Avantages**:
- Debugger intégré
- Breakpoints
- Monitoring en temps réel

---

## 🔧 Variables d'environnement à configurer

**Avant de démarrer**, définir les variables:

### Windows PowerShell
```powershell
# FastAPI
$env:FASTAPI_BASE_URL = "http://localhost:8000"
$env:FASTAPI_INTERNAL_API_KEY = "your-secure-api-key"
$env:FASTAPI_CDW_REPORT_ENDPOINT = "/agents/cdw-report"

# Azure Blob Storage
$env:AZURE_BLOB_STORAGE_CONNECTION_STRING = "<your-azure-blob-storage-connection-string>"
$env:AZURE_BLOB_CONTAINER_NAME = "reports"

# MongoDB (déjà configuré par défaut)
# $env:MONGODB_URI = "mongodb+srv://user:pass@cluster.mongodb.net/db?appName=Cluster01"

# JWT (déjà configuré par défaut)
# $env:JWT_SECRET = "your-jwt-secret-key-here"
```

### Windows CMD
```batch
set FASTAPI_BASE_URL=http://localhost:8000
set FASTAPI_INTERNAL_API_KEY=your-secure-api-key
set AZURE_BLOB_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
set AZURE_BLOB_CONTAINER_NAME=reports
```

### Linux/Mac
```bash
export FASTAPI_BASE_URL=http://localhost:8000
export FASTAPI_INTERNAL_API_KEY=your-secure-api-key
export AZURE_BLOB_CONNECTION_STRING="DefaultEndpointsProtocol=https;..."
export AZURE_BLOB_CONTAINER_NAME=reports
```

---

## ✅ Vérifier que l'application démarre

### Test 1: Swagger UI
```bash
# Via PowerShell
Invoke-WebRequest -Uri "http://localhost:8080/api/swagger-ui.html" -UseBasicParsing | Select-Object StatusCode

# Résultat attendu: 200
```

### Test 2: Health Check
```bash
# Via cURL
curl http://localhost:8080/api/health

# Résultat attendu: application is running
```

### Test 3: API Info
```bash
# Via PowerShell
Invoke-WebRequest -Uri "http://localhost:8080/api/v3/api-docs" -UseBasicParsing | Select-Object StatusCode

# Résultat attendu: 200
```

---

## 📊 Ports et endpoints

| Service | URL | Status |
|---------|-----|--------|
| **API** | http://localhost:8080/api | ✅ Principal |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html | 📖 Documentation |
| **OpenAPI Docs** | http://localhost:8080/api/v3/api-docs | 📄 JSON Spec |
| **CDW Reports** | http://localhost:8080/api/cdw-reports/* | 🎯 Endpoint |

---

## 🧪 Tester l'endpoint CDW Report

### Via PowerShell (Recommandé)
```powershell
# Utiliser le script fourni
.\test-cdw-report-generation.ps1 `
  -FilePath "C:\path\to\test.xlsx" `
  -ProjectId "test-proj-001" `
  -GeneratedBy "test@example.com" `
  -BaseUrl "http://localhost:8080"
```

### Via cURL
```bash
curl -X POST http://localhost:8080/api/cdw-reports/generate-from-upload \
  -F "file=@C:/path/to/test.xlsx" \
  -F "projectId=test-proj-001" \
  -F "generatedBy=test@example.com" \
  -v
```

### Via Postman
1. Méthode: POST
2. URL: `http://localhost:8080/api/cdw-reports/generate-from-upload`
3. Body → Form Data:
   - Key: `file`, Type: File, Value: Select Excel file
   - Key: `projectId`, Type: Text, Value: `test-proj-001`
   - Key: `generatedBy`, Type: Text, Value: `test@example.com`
4. Send

---

## 📝 Logs en temps réel

### Voir les logs pendant l'exécution
```bash
# Via Maven (automatique)
mvn spring-boot:run

# Via jar (affiche dans console)
java -jar target/auditit-1.0.0.jar

# Voir le fichier de log
tail -f app.log  # Linux/Mac
Get-Content app.log -Wait  # PowerShell
```

### Filtrer les logs
```bash
# Erreurs uniquement
grep ERROR app.log

# CDW report logs
grep CDW app.log

# FastAPI communication
grep FastAPI app.log
```

---

## 🛑 Arrêter l'application

### Si démarrée via Maven ou JAR
```
Appuyer sur: Ctrl + C
```

### Si démarrée via IDE
```
Cliquer sur: Stop button dans l'IDE
```

### Via PowerShell (si besoin de forcer)
```powershell
Get-Process java | Stop-Process -Force
```

---

## 🔍 Dépannage au démarrage

### Problème: Port 8080 déjà utilisé
```
Solution 1: Changer le port dans application.properties
server.port=8081

Solution 2: Tuer le processus existant
Get-Process java | Stop-Process -Force
```

### Problème: FastAPI non accessible
```
Vérifier que FastAPI s'exécute:
curl http://localhost:8000/health

Si erreur: Démarrer FastAPI d'abord
```

### Problème: MongoDB connection error
```
Vérifier MONGODB_URI:
echo $env:MONGODB_URI

Vérifier la connectivité:
Ping vers le cluster MongoDB
```

### Problème: Azure Blob Storage error
```
Vérifier la connection string:
echo $env:AZURE_BLOB_CONNECTION_STRING

Vérifier les permissions dans Azure Portal
```

---

## 📋 Checklist avant de démarrer

- [ ] FastAPI service est en cours d'exécution
- [ ] MongoDB Atlas est accessible
- [ ] Azure Blob Storage est configuré
- [ ] Variables d'environnement sont définies
- [ ] JAR est compilé: `target/auditit-1.0.0.jar`
- [ ] Port 8080 est disponible
- [ ] Fichier Excel de test est préparé

---

## 🎯 Prochaines étapes après démarrage

1. **Tester l'API** (5 min)
   → Exécuter le script PowerShell

2. **Vérifier les logs** (5 min)
   → Voir qu'aucune erreur

3. **Vérifier MongoDB** (5 min)
   → Voir les documents créés

4. **Vérifier Blob Storage** (5 min)
   → Voir le fichier uploadé

5. **Intégrer JWT** (30 min)
   → Sécuriser l'endpoint

6. **Ajouter tests** (1-2 heures)
   → Tests unitaires et intégration

---

## 📞 Resources

- **Quick Start Guide**: CDW_REPORT_QUICKSTART.md
- **API Documentation**: CDW_REPORT_API_REFERENCE.md
- **Testing Guide**: CDW_REPORT_TESTING.md
- **Architecture**: CDW_REPORT_ARCHITECTURE.md
- **Next Steps**: CDW_REPORT_NEXT_STEPS.md

---

## ✨ Status

✅ **Application compilée**  
✅ **JAR généré**  
✅ **Prête à être lancée**  
✅ **Production-ready**  

**Date**: 2026-04-23  
**Version**: 1.0.0  


