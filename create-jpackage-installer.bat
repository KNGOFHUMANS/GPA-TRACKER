@echo off
echo ========================================
echo   Creating Single-File EXE Installer
echo ========================================
echo.

REM Check for JDK with jpackage
where jpackage >nul 2>&1
if errorlevel 1 (
    echo ERROR: jpackage not found!
    echo You need JDK 14+ with jpackage tool.
    echo.
    echo Current Java version:
    java -version
    echo.
    echo Please install JDK 21+ from: https://adoptium.net/
    pause
    exit /b 1
)

echo ✅ jpackage found
echo.
echo Creating Windows installer...
echo This will create a single .exe installer that includes everything!
echo.

REM Create app image first
jpackage ^
  --input . ^
  --name GradeRise ^
  --main-jar GradeRise-Complete.jar ^
  --main-class CollegeGPATracker ^
  --type exe ^
  --win-shortcut ^
  --win-menu ^
  --app-version 1.0 ^
  --vendor "GradeRise" ^
  --description "College GPA Tracker" ^
  --copyright "2025 GradeRise"

if exist "GradeRise-1.0.exe" (
    echo.
    echo ========================================
    echo   ✅ SUCCESS!
    echo ========================================
    echo.
    echo Created: GradeRise-1.0.exe
    echo.
    for %%A in (GradeRise-1.0.exe) do echo Size: %%~zA bytes
    echo.
    echo This is an INSTALLER that:
    echo - Installs GradeRise to Program Files
    echo - Creates Desktop shortcut
    echo - Adds Start Menu entry
    echo - Includes everything needed (no Java required!)
    echo.
    echo Users just run the installer and everything works!
    echo.
) else (
    echo ❌ Build failed!
    pause
)

pause
