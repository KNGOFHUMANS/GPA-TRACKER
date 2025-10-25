@echo off
echo.
echo ====================================
echo   GradeRise - GPA Management Tool
echo ====================================
echo.
echo Starting application...
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java 8 or higher and try again.
    echo Download from: https://www.java.com/download/
    echo.
    pause
    exit /b 1
)

REM Run the application
echo Launching GradeRise...
java -jar GradeRise-Complete-Distribution.jar

REM If we get here, the application has closed
echo.
echo Application closed.
pause