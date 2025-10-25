@echo off
echo ========================================
echo    GradeRise EXE Builder with Launch4j
echo ========================================
echo.

REM Check if Launch4j is installed in common locations
set LAUNCH4J_PATH=
if exist "C:\Program Files (x86)\Launch4j\launch4j.exe" set LAUNCH4J_PATH=C:\Program Files (x86)\Launch4j\launch4j.exe
if exist "C:\Program Files\Launch4j\launch4j.exe" set LAUNCH4J_PATH=C:\Program Files\Launch4j\launch4j.exe
if exist "C:\Launch4j\launch4j.exe" set LAUNCH4J_PATH=C:\Launch4j\launch4j.exe

if "%LAUNCH4J_PATH%"=="" (
    echo ❌ Launch4j not found in common installation locations.
    echo.
    echo Please download Launch4j from: https://launch4j.sourceforge.net/
    echo Install it and run this script again.
    echo.
    echo Or manually open Launch4j and load the launch4j-config.xml file.
    pause
    exit /b 1
)

echo ✅ Found Launch4j at: %LAUNCH4J_PATH%
echo.

REM Check if required files exist
if not exist "CollegeGPAApp.jar" (
    echo ❌ CollegeGPAApp.jar not found!
    echo Please make sure you've compiled and created the JAR file first.
    pause
    exit /b 1
)

if not exist "custom-jre" (
    echo ❌ custom-jre folder not found!
    echo Please make sure the bundled JRE is in this folder.
    pause
    exit /b 1
)

if not exist "graderise-icon.png" (
    echo ⚠️  Icon file not found, but continuing anyway...
    echo You can create it by running: java CreateIcon
)

echo ✅ All required files found!
echo.

echo 🚀 Launching Launch4j with GradeRise configuration...
echo.

REM Launch Launch4j with our configuration
"%LAUNCH4J_PATH%" launch4j-config.xml

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ EXE creation completed!
    echo.
    if exist "GradeRise.exe" (
        echo 🎉 GradeRise.exe has been created successfully!
        echo.
        echo 📁 File location: %CD%\GradeRise.exe
        echo 📊 File size: 
        dir "GradeRise.exe" | find "GradeRise.exe"
        echo.
        echo 🧪 Testing the EXE...
        echo Running GradeRise.exe for 3 seconds to test startup...
        timeout /t 1 /nobreak >nul
        start /b "" "GradeRise.exe"
        timeout /t 3 /nobreak >nul
        taskkill /f /im "GradeRise.exe" >nul 2>&1
        echo.
        if exist "graderise-debug.log" (
            echo ✅ Debug log created - startup appears successful!
            echo 📋 Debug log contents:
            echo ----------------------------------------
            type "graderise-debug.log"
            echo ----------------------------------------
        ) else (
            echo ⚠️  No debug log found - there may be an issue with the EXE
        )
        echo.
        echo 💡 Your GradeRise EXE is ready to use!
        echo    Double-click GradeRise.exe to launch the application.
        echo.
    ) else (
        echo ❌ EXE file was not created. Check Launch4j for errors.
    )
) else (
    echo ❌ Launch4j encountered an error during EXE creation.
    echo Please check the Launch4j window for error details.
)

echo.
echo Press any key to exit...
pause >nul