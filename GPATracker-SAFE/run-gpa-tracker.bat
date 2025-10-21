@echo off
echo.
echo ========================================
echo    GPA Tracker v2.0 - Professional
echo ========================================
echo.
echo Starting application...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or later from:
    echo https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Run the application
echo Running GPA Tracker...
java -cp "GPATracker-v2.0.jar;libs/*" CollegeGPATracker

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to start
    echo Make sure all files are present and Java is properly installed
    pause
)