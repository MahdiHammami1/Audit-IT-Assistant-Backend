#!/bin/bash

# ============================================================
# AUDITIT BACKEND - ENTRA ID SETUP SCRIPT
# ============================================================
#
# This script sets up environment variables for Microsoft Entra ID authentication
#
# Usage: source ./setup-entra-id.sh
#
# Then run the backend:
#   mvn spring-boot:run
#

echo "======================================"
echo "AuditIT - Microsoft Entra ID Setup"
echo "======================================"

# Prompt user for Entra ID configuration
echo ""
echo "Enter your Microsoft Entra ID configuration:"
echo ""

read -p "Enter Client ID: " CLIENT_ID
read -p "Enter Client Secret: " CLIENT_SECRET
read -p "Enter Redirect URI [http://localhost:8080/api/entra/callback]: " REDIRECT_URI
REDIRECT_URI=${REDIRECT_URI:-http://localhost:8080/api/entra/callback}

read -p "Enter Metadata URL: " METADATA_URL
read -p "Enter Post Logout Redirect URI [http://localhost:5173]: " POST_LOGOUT_URI
POST_LOGOUT_URI=${POST_LOGOUT_URI:-http://localhost:5173}

read -p "Enter Tenant ID (optional or press Enter to skip): " TENANT_ID

# Export environment variables
export AUTH_ENTRA_CLIENT_ID=$CLIENT_ID
export AUTH_ENTRA_CLIENT_SECRET=$CLIENT_SECRET
export AUTH_ENTRA_REDIRECT_URI=$REDIRECT_URI
export AUTH_ENTRA_METADATA_URL=$METADATA_URL
export AUTH_ENTRA_POST_LOGOUT_REDIRECT_URI=$POST_LOGOUT_URI

if [ -n "$TENANT_ID" ]; then
  export AUTH_ENTRA_TENANT_ID=$TENANT_ID
fi

# Enable security
export SECURITY_ENABLED=true

echo ""
echo "======================================"
echo "Environment variables set successfully!"
echo "======================================"
echo ""
echo "Configuration:"
echo "  CLIENT_ID: $CLIENT_ID"
echo "  REDIRECT_URI: $REDIRECT_URI"
echo "  METADATA_URL: $METADATA_URL"
echo "  POST_LOGOUT_URI: $POST_LOGOUT_URI"
if [ -n "$TENANT_ID" ]; then
  echo "  TENANT_ID: $TENANT_ID"
fi
echo ""
echo "You can now run the backend:"
echo "  mvn spring-boot:run"
echo ""

