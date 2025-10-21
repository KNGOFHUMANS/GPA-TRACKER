@echo off
setlocal EnableDelayedExpansion

title GPA Tracker v2.1 - Portable Edition
color 0A

echo.
echo  ╔═══════════════════════════════════════════════════════╗
echo  ║              GPA TRACKER v2.1 PORTABLE               ║
echo  ║          Single-File Executable Edition               ║
echo  ╚═══════════════════════════════════════════════════════╝
echo.

REM Check for Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed or not in PATH
    echo.
    echo Please install Java from: https://www.java.com/download/
    echo Then try running this application again.
    echo.
    pause
    exit /b 1
)

echo ✅ Java detected - Starting GPA Tracker...
echo 🎓 Loading with custom red graduation cap icon...
echo.

REM Create temporary directory
set "TEMP_DIR=%TEMP%\GPATracker_%RANDOM%"
mkdir "%TEMP_DIR%" 2>nul
mkdir "%TEMP_DIR%\libs" 2>nul

REM Copy embedded files (this would be done by a self-extracting archive)
echo Extracting application files...
copy "GPATracker-v2.0.jar" "%TEMP_DIR%\" >nul 2>&1
copy "app-icon*.png" "%TEMP_DIR%\" >nul 2>&1
copy "app-icon.ico" "%TEMP_DIR%\" >nul 2>&1
copy "client_secret.json.template" "%TEMP_DIR%\" >nul 2>&1
xcopy "libs" "%TEMP_DIR%\libs\" /E /Q >nul 2>&1

if not exist "%TEMP_DIR%\GPATracker-v2.0.jar" (
    echo ❌ Failed to extract application files
    echo Make sure all files are in the same directory as this launcher.
    pause
    exit /b 1
)

echo ✅ Files extracted successfully
echo 🚀 Launching GPA Tracker...

REM Launch the application
cd /d "%TEMP_DIR%"
start "GPA Tracker" java -cp "GPATracker-v2.0.jar;libs/*" CollegeGPATracker

REM Wait a moment then clean up (optional - comment out for debugging)
timeout /t 3 >nul
rd /s /q "%TEMP_DIR%" 2>nul

exit /b 0