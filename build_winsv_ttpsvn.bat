@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

for /f "tokens=*" %%a in ('echo prompt $E^| cmd') do set "ESC=%%a"
set "CYAN=%ESC%[36m"
set "GREEN=%ESC%[32m"
set "YELLOW=%ESC%[33m"
set "RED=%ESC%[31m"
set "RESET=%ESC%[0m"

:: ============================================================
::  BUILD SCRIPT: winsv_ttpsvn
:: ============================================================

set "ROOT_DIR=%~dp0"
set "MODULE_DIR=%ROOT_DIR%winsv_ttpsvn"
set "TARGET_DIR=%MODULE_DIR%\target"
set "RUNTIME_DIR=%MODULE_DIR%\runtime"
set "DIST_DIR=%MODULE_DIR%\dist"
set "APP_NAME=winsv_ttpsvn"
set "JAR_NAME=winsv_ttpsvn-1.0.0.jar"
set "DEPLOY_DIR=%ROOT_DIR%tp-net-trigger"
set "JVM_MODULES=java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.security.jgss,java.sql,jdk.attach,jdk.jdi"

echo.
echo %CYAN%===============================================%RESET%
echo %CYAN%  BUILD: %APP_NAME%%RESET%
echo %CYAN%===============================================%RESET%
echo.

:: -------------------------------------------------------
:: STEP 1: Maven clean package
:: -------------------------------------------------------
echo %CYAN%[INFO]%RESET% [1/5] Maven clean package...
echo.
cd /d "%ROOT_DIR%"
call mvn clean package -pl winsv_ttpsvn -am -DskipTests
if errorlevel 1 (
    echo.
    echo %RED%[FAILED]%RESET% Maven build that bai!
    pause & exit /b 1
)
echo.
echo %GREEN%[OK]%RESET% Maven build thanh cong ^> %JAR_NAME%

:: -------------------------------------------------------
:: STEP 2: Xoa runtime va dist cu
:: -------------------------------------------------------
echo.
echo %CYAN%[INFO]%RESET% [2/5] Don dep runtime va dist cu...
if exist "%RUNTIME_DIR%" (
    rmdir /s /q "%RUNTIME_DIR%"
    echo %CYAN%[INFO]%RESET% Removed: %RUNTIME_DIR%
)
if exist "%DIST_DIR%" (
    rmdir /s /q "%DIST_DIR%"
    echo %CYAN%[INFO]%RESET% Removed: %DIST_DIR%
)

:: -------------------------------------------------------
:: STEP 3: jlink - tao custom JRE runtime
:: -------------------------------------------------------
echo.
echo %CYAN%[INFO]%RESET% [3/5] jlink - tao custom JRE runtime...
echo.
jlink ^
  --module-path "%JAVA_HOME%\jmods" ^
  --add-modules %JVM_MODULES% ^
  --output "%RUNTIME_DIR%" ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2

if errorlevel 1 (
    echo.
    echo %RED%[FAILED]%RESET% jlink that bai! Kiem tra JAVA_HOME: %JAVA_HOME%
    pause & exit /b 1
)
echo.
echo %GREEN%[OK]%RESET% Runtime tao thanh cong: runtime\

:: -------------------------------------------------------
:: STEP 4: jpackage - dong goi app-image
:: -------------------------------------------------------
echo.
echo %CYAN%[INFO]%RESET% [4/5] jpackage - dong goi app-image...
echo.
cd /d "%MODULE_DIR%"
jpackage ^
  --type app-image ^
  --name %APP_NAME% ^
  --input "%TARGET_DIR%" ^
  --main-jar %JAR_NAME% ^
  --runtime-image "%RUNTIME_DIR%" ^
  --dest "%DIST_DIR%" ^
  --win-console

if errorlevel 1 (
    echo.
    echo %RED%[FAILED]%RESET% jpackage that bai!
    pause & exit /b 1
)
echo.
echo %GREEN%[OK]%RESET% App image: dist\%APP_NAME%\

:: -------------------------------------------------------
:: STEP 5: Copy .env va run.bat vao dist
:: -------------------------------------------------------
echo.
echo %CYAN%[INFO]%RESET% [5/5] Copy .env va run.bat vao dist...
set "APP_DIR=%DIST_DIR%\%APP_NAME%"

set "ENV_SRC=%MODULE_DIR%\src\main\resources\.env"
if exist "%ENV_SRC%" (
    copy /y "%ENV_SRC%" "%APP_DIR%\.env" > nul
    echo %GREEN%[OK]%RESET% .env sao chep thanh cong
) else (
    echo %YELLOW%[WARN]%RESET% Khong tim thay .env tai: %ENV_SRC%
    echo %YELLOW%[WARN]%RESET% Hay tu dat .env vao: %APP_DIR%\
)

set "RUN_SRC=%ROOT_DIR%batFile\run.bat"
if exist "%RUN_SRC%" (
    copy /y "%RUN_SRC%" "%APP_DIR%\run.bat" > nul
    echo %GREEN%[OK]%RESET% run.bat sao chep thanh cong
) else (
    echo %YELLOW%[WARN]%RESET% Khong tim thay run.bat tai: %RUN_SRC%
    echo %YELLOW%[WARN]%RESET% Hay dat file vao: %ROOT_DIR%batFile\run.bat
)

:: -------------------------------------------------------
:: STEP 6: Deploy va don dep
:: -------------------------------------------------------
echo.
echo %CYAN%[INFO]%RESET% [6/6] Deploy va don dep...
if exist "%DEPLOY_DIR%" rmdir /s /q "%DEPLOY_DIR%"
xcopy "%APP_DIR%" "%DEPLOY_DIR%\" /e /i /q
if errorlevel 1 (
    echo %RED%[FAILED]%RESET% Deploy that bai!
    pause & exit /b 1
)
echo %GREEN%[OK]%RESET% Deployed to: tp-net-trigger\

rmdir /s /q "%DIST_DIR%"
rmdir /s /q "%RUNTIME_DIR%"
rmdir /s /q "%TARGET_DIR%"

echo %GREEN%[OK]%RESET% Temp files cleaned.

:: -------------------------------------------------------
:: HOAN THANH
:: -------------------------------------------------------
echo.
echo %GREEN%===============================================%RESET%
echo %GREEN%  BUILD HOAN THANH!%RESET%
echo %GREEN%===============================================%RESET%
echo.
echo %CYAN%[INFO]%RESET% Output: %DEPLOY_DIR%\
echo.
echo %CYAN%[INFO]%RESET% Cau truc:
echo         tp-net-trigger\
echo         ^|-- app\
echo         ^|-- runtime\
echo         ^|-- %APP_NAME%.exe
echo         ^|-- .env
echo         ^`-- run.bat
echo.

pause
endlocal