#!/usr/bin/env pwsh

# Configuration
$BASE_URL = "http://localhost:8080/api"
$TOKEN = ""

# Couleurs pour l'output
function Write-Success {
    param([string]$message)
    Write-Host "✓ $message" -ForegroundColor Green
}

function Write-ErrorMsg {
    param([string]$message)
    Write-Host "✗ $message" -ForegroundColor Red
}

function Write-InfoMsg {
    param([string]$message)
    Write-Host "ℹ $message" -ForegroundColor Cyan
}

# ====== Authentication ======
Write-InfoMsg "====== Getting Auth Token ======"

try {
    $signupBody = @{
        email = "testadmin@example.com"
        password = "TestAdmin@123456"
        firstName = "Test"
        lastName = "Admin"
    } | ConvertTo-Json

    $signupResponse = Invoke-WebRequest -Uri "$BASE_URL/auth/signup" -Method POST -Headers @{"Content-Type"="application/json"} -Body $signupBody -ErrorAction Stop
    Write-Success "Signup successful"
} catch {
    Write-InfoMsg "User might already exist, continuing..."
}

try {
    $signinBody = @{
        email = "testadmin@example.com"
        password = "TestAdmin@123456"
    } | ConvertTo-Json

    $signinResponse = Invoke-WebRequest -Uri "$BASE_URL/auth/signin" -Method POST -Headers @{"Content-Type"="application/json"} -Body $signinBody -ErrorAction Stop
    $signinData = $signinResponse.Content | ConvertFrom-Json
    $TOKEN = $signinData.data.token
    Write-Success "Signin successful - Token: $($TOKEN.Substring(0, 20))..."
} catch {
    Write-ErrorMsg "Signin failed: $($_.Exception.Message)"
    exit 1
}

# ====== ITGC Domains Tests ======
Write-InfoMsg "====== Testing ITGC Domains Endpoints ======"

# GET all domains
Write-InfoMsg "Testing: GET /itgc-domains"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/itgc-domains" -Method GET -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "GET /itgc-domains - OK"
    } else {
        Write-ErrorMsg "GET /itgc-domains - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "GET /itgc-domains - Exception: $($_.Exception.Message)"
}

# POST bulk create domains
Write-InfoMsg "Testing: POST /itgc-domains/bulk"
try {
    $body = @(
        @{
            code = "APD"
            name = "Application Development"
            description = "Application Development Domain"
        },
        @{
            code = "SYS"
            name = "Systems Management"
            description = "Systems Management Domain"
        }
    ) | ConvertTo-Json

    $response = Invoke-WebRequest -Uri "$BASE_URL/itgc-domains/bulk" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $TOKEN"} -Body $body -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "POST /itgc-domains/bulk - OK"
    } else {
        Write-ErrorMsg "POST /itgc-domains/bulk - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "POST /itgc-domains/bulk - Exception: $($_.Exception.Message)"
}

# POST bulk create controls
Write-InfoMsg "Testing: POST /itgc-domains/controls/bulk"
try {
    $body = @{
        domainCode = "APD"
        controls = @(
            @{
                code = "CTRL-001"
                title = "Code Review"
                description = "Code review control"
                orderIndex = 1
            },
            @{
                code = "CTRL-002"
                title = "Testing"
                description = "Testing control"
                orderIndex = 2
            }
        )
    } | ConvertTo-Json -Depth 3

    $response = Invoke-WebRequest -Uri "$BASE_URL/itgc-domains/controls/bulk" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $TOKEN"} -Body $body -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "POST /itgc-domains/controls/bulk - OK"
    } else {
        Write-ErrorMsg "POST /itgc-domains/controls/bulk - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "POST /itgc-domains/controls/bulk - Exception: $($_.Exception.Message)"
}

# GET controls by domain
Write-InfoMsg "Testing: GET /itgc-domains/APD/controls"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/itgc-domains/APD/controls" -Method GET -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "GET /itgc-domains/APD/controls - OK"
    } else {
        Write-ErrorMsg "GET /itgc-domains/APD/controls - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "GET /itgc-domains/APD/controls - Exception: $($_.Exception.Message)"
}

# DELETE all domains
Write-InfoMsg "Testing: DELETE /itgc-domains/all"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/itgc-domains/all" -Method DELETE -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "DELETE /itgc-domains/all - OK"
    } else {
        Write-ErrorMsg "DELETE /itgc-domains/all - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "DELETE /itgc-domains/all - Exception: $($_.Exception.Message)"
}

# ====== Profile Tests ======
Write-InfoMsg "====== Testing Profile Endpoints ======"

# GET all profiles
Write-InfoMsg "Testing: GET /profiles"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/profiles" -Method GET -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "GET /profiles - OK"
    } else {
        Write-ErrorMsg "GET /profiles - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "GET /profiles - Exception: $($_.Exception.Message)"
}

# GET current profile
Write-InfoMsg "Testing: GET /profiles/me"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/profiles/me" -Method GET -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "GET /profiles/me - OK"
    } else {
        Write-ErrorMsg "GET /profiles/me - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "GET /profiles/me - Exception: $($_.Exception.Message)"
}

# GET all auditors
Write-InfoMsg "Testing: GET /profiles/auditors"
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/profiles/auditors" -Method GET -Headers @{"Authorization"="Bearer $TOKEN"} -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Success "GET /profiles/auditors - OK"
    } else {
        Write-ErrorMsg "GET /profiles/auditors - Failed with status $($response.StatusCode)"
    }
} catch {
    Write-ErrorMsg "GET /profiles/auditors - Exception: $($_.Exception.Message)"
}

Write-InfoMsg "====== All Tests Completed ======"

