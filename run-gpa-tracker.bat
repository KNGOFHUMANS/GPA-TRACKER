@echo off
title GPA Tracker v2.1
echo.
echo ========================================
echo    GPA Tracker v2.1 - With Red Icon
echo ========================================
echo.
echo Starting application...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java from: https://www.java.com/download/
    pause
    exit /b 1
)

REM Run the application with red graduation cap icon
echo Running GPA Tracker with Red Graduation Cap Icon...
java -cp "GPATracker-v2.0.jar;libs/*" CollegeGPATracker

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to start
    echo Make sure all files are present and Java is properly installed
    pause
)