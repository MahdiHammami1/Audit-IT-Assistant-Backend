# Script de test des endpoints d'authentification
# Usage: .\test-auth-endpoints.ps1 -token "votre_token_jwt"

param(
    [string]$token = "",
    [string]$baseUrl = "http://localhost:8080/api"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test des Endpoints d'Authentification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Colors
$successColor = "Green"
$errorColor = "Red"
$warningColor = "Yellow"

# Function to make HTTP requests
function Test-Endpoint {
    param(
        [string]$method = "GET",
        [string]$endpoint,
        [string]$bearerToken = "",
        [hashtable]$headers = @{}
    )

    $url = "${baseUrl}${endpoint}"
    $requestHeaders = @{
        "Content-Type" = "application/json"
    }

    # Add Authorization header if token is provided
    if ($bearerToken) {
        $requestHeaders["Authorization"] = "Bearer ${bearerToken}"
    }

    # Merge custom headers
    $headers.GetEnumerator() | ForEach-Object {
        $requestHeaders[$_.Key] = $_.Value
    }

    Write-Host "Test: $method $endpoint" -ForegroundColor Cyan

    try {
        $response = Invoke-WebRequest -Uri $url -Method $method -Headers $requestHeaders -ErrorAction Stop
        Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor $successColor
        Write-Host "Response:" -ForegroundColor $successColor
        Write-Host ($response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3) -ForegroundColor $successColor
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "❌ Status: $statusCode" -ForegroundColor $errorColor
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor $errorColor

        try {
            $errorContent = $_.Exception.Response.Content.ReadAsStream() | {
                param($stream)
                $reader = [System.IO.StreamReader]::new($stream)
                $reader.ReadToEnd()
            }
            Write-Host "Error Response:" -ForegroundColor $errorColor
            Write-Host ($errorContent | ConvertFrom-Json | ConvertTo-Json -Depth 3) -ForegroundColor $errorColor
        }
        catch {
            # Silently fail if we can't parse the error response
        }
    }

    Write-Host ""
}

# Test 1: Check if server is running
Write-Host "1. Vérification du serveur..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "${baseUrl}/actuator/health" -ErrorAction Stop
    Write-Host "✅ Serveur est accessible" -ForegroundColor $successColor
}
catch {
    Write-Host "❌ Serveur n'est pas accessible à ${baseUrl}" -ForegroundColor $errorColor
    Write-Host "Assurez-vous que le serveur est started et running sur le port 8080" -ForegroundColor $warningColor
    exit
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests SANS TOKEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 2: CORS test (no token needed)
Test-Endpoint -endpoint "/auth/debug/test-cors"

# Test 3: Test auth header (no token)
Test-Endpoint -endpoint "/auth/debug/test-auth"

if ($token) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Tests AVEC TOKEN" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    # Show token info
    Write-Host "Token utilisé: ${token:0:20}...${token:(-20)}" -ForegroundColor Yellow
    Write-Host ""

    # Test 4: Test auth header (with token)
    Test-Endpoint -endpoint "/auth/debug/test-auth" -bearerToken $token

    # Test 5: Test current user
    Test-Endpoint -endpoint "/auth/debug/test-current-user" -bearerToken $token

    # Test 6: Get missions
    Test-Endpoint -endpoint "/missions" -bearerToken $token

    # Test 7: Get user profile
    Test-Endpoint -endpoint "/profiles/me" -bearerToken $token

    # Test 8: Get all profiles
    Test-Endpoint -endpoint "/profiles" -bearerToken $token
}
else {
    Write-Host ""
    Write-Host "ℹ️  Pour tester avec un token JWT, exécutez:" -ForegroundColor Cyan
    Write-Host "   .\test-auth-endpoints.ps1 -token 'votre_jwt_token'" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Pour obtenir un token JWT:" -ForegroundColor Yellow
    Write-Host "   1. Allez à http://localhost:5173 et connectez-vous" -ForegroundColor Yellow
    Write-Host "   2. Ouvrez Developer Tools (F12) -> Console" -ForegroundColor Yellow
    Write-Host "   3. Exécutez: localStorage.getItem('token')" -ForegroundColor Yellow
    Write-Host "   4. Copiez le token et passez-le à ce script" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests Terminés" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

