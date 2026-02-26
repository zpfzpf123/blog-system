@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Auto-start backend/frontend services for this project.
REM Run with --dry-run to print start commands without launching processes.

set "PROJECT_ROOT=%~dp0"
for %%I in ("%PROJECT_ROOT%.") do set "PROJECT_ROOT=%%~fI"
set "BACKEND_DIR=%PROJECT_ROOT%\backend-spring"
set "FRONTEND_DIR=%PROJECT_ROOT%"
set "BACKEND_PORT=4567"
set "FRONTEND_PORT=5173"

if /I "%~1"=="--dry-run" set "DRY_RUN=1"

echo [INFO] Project root: "%PROJECT_ROOT%"

if not exist "%BACKEND_DIR%\pom.xml" (
  echo [ERROR] Backend project not found: "%BACKEND_DIR%"
  exit /b 1
)

if not exist "%FRONTEND_DIR%\package.json" (
  echo [ERROR] Frontend project not found: "%FRONTEND_DIR%"
  exit /b 1
)

call :check_command mvn || exit /b 1
call :check_command npm || exit /b 1

call :start_if_needed "AI-Blog Backend" "%BACKEND_DIR%" "mvn spring-boot:run" %BACKEND_PORT%
call :start_if_needed "AI-Blog Frontend" "%FRONTEND_DIR%" "npm run dev" %FRONTEND_PORT%

echo [OK] Auto-start sequence finished.
exit /b 0

:wait_port
set "WAIT_PORT=%~1"
set "WAIT_RETRIES=%~2"
for /L %%S in (1,1,!WAIT_RETRIES!) do (
  call :is_port_listening !WAIT_PORT!
  if "!PORT_LISTENING!"=="1" exit /b 0
  >nul ping 127.0.0.1 -n 2
)
echo [WARN] Port !WAIT_PORT! did not become ready in !WAIT_RETRIES! seconds.
exit /b 0

:check_command
where %~1 >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Required command not found in PATH: %~1
  exit /b 1
)
exit /b 0

:start_if_needed
set "WINDOW_TITLE=%~1"
set "SERVICE_DIR=%~2"
set "SERVICE_CMD=%~3"
set "SERVICE_PORT=%~4"

call :is_port_listening %SERVICE_PORT%
if "!PORT_LISTENING!"=="1" (
  echo [INFO] Port !SERVICE_PORT! already listening. Skip !WINDOW_TITLE!.
  exit /b 0
)

if defined DRY_RUN (
  echo [DRY-RUN] start "!WINDOW_TITLE!" /MIN cmd /k "cd /d ""!SERVICE_DIR!"" ^&^& !SERVICE_CMD!"
  exit /b 0
)

echo [INFO] Starting !WINDOW_TITLE!...
start "!WINDOW_TITLE!" /MIN cmd /k "cd /d ""!SERVICE_DIR!"" && !SERVICE_CMD!"
exit /b 0

:is_port_listening
set "PORT_LISTENING=0"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:":%1 .*LISTENING"') do (
  set "PORT_LISTENING=1"
  goto :eof
)
goto :eof
