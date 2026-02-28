@echo off
setlocal
cd /d "%~dp0"

set PORT=1881

echo [cloudflared] loop started (port %PORT%)

:CF_LOOP

REM === check if port already in use ===
netstat -ano | find ":%PORT%" >nul
if %errorlevel%==0 (
    echo [cloudflared] port %PORT% already in use, skip start
    powershell -Command "Start-Sleep -Seconds 5"
    goto CF_LOOP
)

echo [cloudflared] starting client...
call "%CD%\cloudflared_module\client_cloudflared.bat"

echo [cloudflared] exited, retry in 10 seconds...
powershell -Command "Start-Sleep -Seconds 10"
goto CF_LOOP
