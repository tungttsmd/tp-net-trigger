@echo off
setlocal

REM =====================================================
REM === SELF ELEVATE TO ADMIN (1 TIME ONLY) ===
REM =====================================================
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ================================
    echo   Dang yeu cau quyen quan tri...
    echo ================================
    echo.
    powershell -Command ^
      "Start-Process '%~f0' -Verb RunAs -ArgumentList '%*'"
    exit /b
)

REM =====================================================
REM === set working dir to current folder ===
REM =====================================================
cd /d "%~dp0"

echo [START] Launching dependencies...



REM =====================================================
REM === CLOUD FLARED : retry every 10s if exit/fail ===
REM =====================================================
echo [START] cloudflared client (with retry)
start "" "%CD%\cloudflared_loop.bat"



REM =====================================================
REM === HARDWARE MONITOR : run as ADMIN (already elevated) ===
REM =====================================================
echo [START] libreHwMonitor (admin)
start "" "%CD%\sensor_module\libreHwMonitor.exe"



REM =====================================================
REM === WAIT BEFORE MAIN SERVICE ===
REM =====================================================
echo [WAIT] Waiting 5 seconds before starting winsv_ttpsvn...
powershell -Command "Start-Sleep -Seconds 5"



REM =====================================================
REM === MAIN SERVICE ===
REM =====================================================
echo [START] winsv_ttpsvn.exe
start "" "%CD%\winsv_ttpsvn.exe"



echo [DONE] All modules launched.
exit /b 0
