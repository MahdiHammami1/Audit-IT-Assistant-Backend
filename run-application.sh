#!/bin/bash
# AuditIT Backend - MongoDB Migration Deployment Script
# Execute this file to build and run the application

echo "=========================================="
echo "  AuditIT Backend - MongoDB Deployment"
echo "=========================================="
echo ""

# Set project directory
PROJECT_DIR="C:\Users\mahdi\Desktop\PFE PWC\auditit-backend"

# Step 1: Navigate to project
echo "[1/5] Navigating to project directory..."
cd "$PROJECT_DIR" || exit 1
echo "✅ Current directory: $PWD"
echo ""

# Step 2: Clean and compile
echo "[2/5] Cleaning and compiling project..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi
echo ""

# Step 3: Build package
echo "[3/5] Building package..."
mvn -DskipTests package -q
if [ $? -eq 0 ]; then
    echo "✅ Package built successfully"
else
    echo "❌ Build failed"
    exit 1
fi
echo ""

# Step 4: Verify JAR
echo "[4/5] Verifying JAR file..."
if [ -f "target/auditit-1.0.0.jar" ]; then
    JAR_SIZE=$(du -h target/auditit-1.0.0.jar | cut -f1)
    echo "✅ JAR file ready: target/auditit-1.0.0.jar ($JAR_SIZE)"
else
    echo "❌ JAR file not found"
    exit 1
fi
echo ""

# Step 5: Run application
echo "[5/5] Starting application..."
echo ""

# Use local MongoDB by default. To use Atlas, set MONGODB_URI before running this script.
if [ -z "$MONGODB_URI" ]; then
    export MONGODB_URI="mongodb://127.0.0.1:27017/auditit"
fi

echo "MongoDB Configuration:"
echo "  URL: $MONGODB_URI"
echo ""
echo "Application will start on:"
echo "  http://localhost:8080/api"
echo "  Swagger UI: http://localhost:8080/api/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop the application"
echo "=========================================="
echo ""

# Run JAR
java -jar target/auditit-1.0.0.jar

echo ""
echo "=========================================="
echo "  Application stopped"
echo "=========================================="
