# ============================================================
# AUDITIT BACKEND - ENTRA ID ENDPOINTS TEST
# ============================================================
#
# PowerShell script to test Microsoft Entra ID authentication endpoints
#
# Prerequisites:
# - Backend running on http://localhost:8080
# - Entra ID configured
#
# Usage: .\test-entra-id.ps1
#

$BaseUrl = "http://localhost:8080/api"
$Headers = @{ "Content-Type" = "application/json" }

Write-Host "======================================"
Write-Host "AuditIT - Entra ID Endpoints Test"
Write-Host "======================================"
Write-Host ""

# Test 1: Get Login URL
Write-Host "Test 1: GET /api/entra/login"
Write-Host "Description: Retrieve Microsoft Entra ID login URL"
Write-Host "----------------------------------------"

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/entra/login" -Method Get -Headers $Headers
    $content = $response.Content | ConvertFrom-Json

    Write-Host "Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Success: $($content.success)"
    Write-Host "Message: $($content.message)"

    if ($content.data.loginUrl) {
        Write-Host ""
        Write-Host "Login URL:" -ForegroundColor Yellow
        Write-Host $content.data.loginUrl
        Write-Host ""
        Write-Host "Next Steps:" -ForegroundColor Cyan
        Write-Host "1. Copy the login URL above"
        Write-Host "2. Open it in your browser"
        Write-Host "3. Sign in with your Microsoft account"
        Write-Host "4. You will be redirected to the callback endpoint"
        Write-Host "5. The callback will return JWT token and user info"
    }

    Write-Host ""
    Write-Host "Full Response:"
    Write-Host ($content | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ToString())" -ForegroundColor Red
}

Write-Host ""
Write-Host ""
Write-Host "Test 2: GET /api/entra/callback (Simulated)"
Write-Host "Description: This endpoint is called by Entra ID after user login"
Write-Host "----------------------------------------"
Write-Host "Note: You must complete the login flow from Test 1 to get a real authorization code"
Write-Host ""
Write-Host "Example URL pattern:"
Write-Host "GET /api/entra/callback?code=<authorization_code>&state=<state>"
Write-Host ""
Write-Host "Expected Response (200 OK):"
Write-Host @"
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "uuid-here",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe"
    },
    "message": "Authentication successful via Microsoft Entra ID"
  }
}
"@

Write-Host ""
Write-Host ""
Write-Host "Test 3: POST /api/entra/logout"
Write-Host "Description: Logout the authenticated user"
Write-Host "----------------------------------------"

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/entra/logout" -Method Post -Headers $Headers
    $content = $response.Content | ConvertFrom-Json

    Write-Host "Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Success: $($content.success)"
    Write-Host "Message: $($content.message)"
    Write-Host ""
    Write-Host "Full Response:"
    Write-Host ($content | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host ""
Write-Host "======================================"
Write-Host "Testing Complete"
Write-Host "======================================"
Write-Host ""
Write-Host "For more information, see README_ENTRA_ID.md"
Write-Host ""

