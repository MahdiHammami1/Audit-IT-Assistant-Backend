# ============================================================
# AUDITIT BACKEND - ENTRA ID SETUP (Windows PowerShell)
# ============================================================
#
# This script sets up environment variables for Microsoft Entra ID authentication
#
# Usage: .\setup-entra-id.ps1
#
# Then run the backend:
#   mvn spring-boot:run
#

Write-Host "======================================"
Write-Host "AuditIT - Microsoft Entra ID Setup"
Write-Host "======================================"
Write-Host ""

# Function to set environment variable
function Set-EnvVar {
    param(
        [string]$Name,
        [string]$Value,
        [string]$Scope = "Process"
    )
    [Environment]::SetEnvironmentVariable($Name, $Value, $Scope)
    Write-Host "  ✓ $Name set" -ForegroundColor Green
}

Write-Host "Enter your Microsoft Entra ID configuration:"
Write-Host ""

$clientId = Read-Host "Enter Client ID"
$clientSecret = Read-Host "Enter Client Secret (will be hidden)"
$redirectUri = Read-Host "Enter Redirect URI [default: http://localhost:8080/api/entra/callback]"
$redirectUri = if ($redirectUri) { $redirectUri } else { "http://localhost:8080/api/entra/callback" }

$metadataUrl = Read-Host "Enter Metadata URL"
$postLogoutUri = Read-Host "Enter Post Logout Redirect URI [default: http://localhost:5173]"
$postLogoutUri = if ($postLogoutUri) { $postLogoutUri } else { "http://localhost:5173" }

$tenantId = Read-Host "Enter Tenant ID (optional, press Enter to skip)"

# Set environment variables for current process
Write-Host ""
Write-Host "Setting environment variables..."
Write-Host ""

Set-EnvVar -Name "AUTH_ENTRA_CLIENT_ID" -Value $clientId
Set-EnvVar -Name "AUTH_ENTRA_CLIENT_SECRET" -Value $clientSecret
Set-EnvVar -Name "AUTH_ENTRA_REDIRECT_URI" -Value $redirectUri
Set-EnvVar -Name "AUTH_ENTRA_METADATA_URL" -Value $metadataUrl
Set-EnvVar -Name "AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI" -Value $postLogoutUri
Set-EnvVar -Name "SECURITY_ENABLED" -Value "true"

if ($tenantId) {
    Set-EnvVar -Name "AUTH_ENTRA_TENANT_ID" -Value $tenantId
}

Write-Host ""
Write-Host "======================================"
Write-Host "Environment variables set successfully!"
Write-Host "======================================"
Write-Host ""
Write-Host "Configuration summary:"
Write-Host "  Client ID: $clientId"
Write-Host "  Redirect URI: $redirectUri"
Write-Host "  Metadata URL: $metadataUrl"
Write-Host "  Post Logout URI: $postLogoutUri"
if ($tenantId) {
    Write-Host "  Tenant ID: $tenantId"
}
Write-Host ""
Write-Host "You can now run the backend:"
Write-Host "  mvn spring-boot:run"
Write-Host ""
Write-Host "Note: These environment variables are set only for the current PowerShell session."
Write-Host "To set permanent environment variables, use:"
Write-Host "  [Environment]::SetEnvironmentVariable('VAR_NAME', 'value', 'User')"
Write-Host ""

