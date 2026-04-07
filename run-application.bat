@echo off
REM AuditIT Backend - MongoDB Migration Deployment Script (Windows)
REM Execute this file to build and run the application

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo   AuditIT Backend - MongoDB Deployment
echo ==========================================
echo.

REM Set project directory
set PROJECT_DIR=C:\Users\mahdi\Desktop\PFE PWC\auditit-backend

REM Step 1: Navigate to project
echo [1/5] Navigating to project directory...
cd /d "%PROJECT_DIR%"
if errorlevel 1 (
    echo Error: Could not navigate to project directory
    exit /b 1
)
echo OK - Current directory: %cd%
echo.

REM Step 2: Clean and compile
echo [2/5] Cleaning and compiling project...
call mvn clean compile -q
if errorlevel 1 (
    echo ERROR: Compilation failed
    exit /b 1
)
echo OK - Compilation successful
echo.

REM Step 3: Build package
echo [3/5] Building package...
call mvn -DskipTests package -q
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)
echo OK - Package built successfully
echo.

REM Step 4: Verify JAR
echo [4/5] Verifying JAR file...
if exist "target\auditit-1.0.0.jar" (
    for /F "usebackq" %%A in ('call powershell -Command "(Get-Item 'target\auditit-1.0.0.jar').length / 1MB | ForEach-Object {'{0:N2} MB' -f $_}"') do set JAR_SIZE=%%A
    echo OK - JAR file ready: target\auditit-1.0.0.jar (!JAR_SIZE!)
) else (
    echo ERROR: JAR file not found
    exit /b 1
)
echo.

REM Step 5: Run application
echo [5/5] Starting application...
echo.
echo MongoDB Configuration:
echo   URL: mongodb+srv://mahdihammami:testtest@cluster01.v7ca8ov.mongodb.net/db1?appName=Cluster01
echo   Database: db1
echo.
echo Application will start on:
echo   http://localhost:8080/api
echo   Swagger UI: http://localhost:8080/api/swagger-ui.html
echo.
echo Press Ctrl+C to stop the application
echo ==========================================
echo.

REM Set MongoDB URI environment variable
set MONGODB_URI=mongodb+srv://mahdihammami:testtest@cluster01.v7ca8ov.mongodb.net/db1?appName=Cluster01

REM Run JAR
java -jar target\auditit-1.0.0.jar

echo.
echo ==========================================
echo   Application stopped
echo ==========================================
pause

