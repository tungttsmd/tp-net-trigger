@echo off
setlocal
cd /d "%~dp0"

echo [KILL] Stopping TTPSVN stack...

REM === kill winsv ===
taskkill /F /IM winsv_ttpsvn*.exe /T >nul 2>&1

REM === kill cloudflared ===
taskkill /F /IM cloudflared*.exe /T >nul 2>&1

REM === kill hw monitor ===
taskkill /F /IM libreHwMonitor*.exe /T >nul 2>&1

REM === kill leftover cmd from loop ===
taskkill /F /IM cmd.exe /T >nul 2>&1

echo [KILL] Done.
exit /b 0
