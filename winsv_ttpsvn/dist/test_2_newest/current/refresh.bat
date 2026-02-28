@echo off
setlocal
cd /d "%~dp0"

echo [REFRESH] Restarting TTPSVN stack...

REM === stop everything ===
call "%CD%\kill.bat"

REM === wait 3 seconds for Windows to release file locks ===
powershell -Command "Start-Sleep -Seconds 3"

REM === start again ===
call "%CD%\start.bat"

echo [REFRESH] Done.
exit /b 0
