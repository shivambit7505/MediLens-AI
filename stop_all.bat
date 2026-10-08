@echo off
title MediLens AI - Stop Services
echo =====================================================================
echo                 Stopping MediLens AI Services
echo =====================================================================
echo.

echo Stopping services listening on port 8000, 8080, and 3000...
powershell -Command "Get-NetTCPConnection -LocalPort 8000,8080,3000 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }"

echo.
echo All MediLens AI services stopped successfully.
timeout /t 3 >nul
