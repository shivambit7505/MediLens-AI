@echo off
title MediLens AI - Service Launcher
echo =====================================================================
echo                Starting MediLens AI Healthcare Platform
echo =====================================================================
echo.

cd /d "%~dp0"

echo [1/3] Starting AI Service (FastAPI on Port 8000)...
start "MediLens AI Service (:8000)" cmd /k "cd ai-service && python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload"

echo [2/3] Starting Backend Service (Spring Boot on Port 8080)...
start "MediLens Backend (:8080)" cmd /k "cd backend && mvn spring-boot:run"

echo [3/3] Starting Frontend (React on Port 3000)...
start "MediLens Frontend (:3000)" cmd /k "cd frontend && npm run dev"

echo.
echo All services launched in their respective windows!
echo - AI Service:  http://localhost:8000/docs
echo - Backend:     http://localhost:8080/actuator/health
echo - Web App:     http://localhost:3000
echo.
echo Opening MediLens AI in your default web browser...
timeout /t 5 >nul
start http://localhost:3000

echo Done! Keep the service windows open while using the app.
