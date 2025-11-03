@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    GradeRise Standalone EXE Builder
echo ========================================
echo.

REM Check if Launch4j exists
if not exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    if not exist "C:\Program Files\Launch4j\launch4jc.exe" (
        echo ERROR: Launch4j not found!
        echo Please install Launch4j or update the path in this script.
        pause
        exit /b 1
    )
    set LAUNCH4J="C:\Program Files\Launch4j\launch4jc.exe"
) else (
    set LAUNCH4J="C:\Program Files (x86)\Launch4j\launch4jc.exe"
)

echo Step 1: Checking JAR file...
if not exist "GradeRise-Complete.jar" (
    echo ERROR: GradeRise-Complete.jar not found!
    echo Please run create-complete-jar.bat first.
    pause
    exit /b 1
)
echo ✅ JAR file found

echo.
echo Step 2: Creating Launch4j configuration...

REM Get absolute path to current directory
set "CURRENT_DIR=%CD%"

REM Check for icon file (optional)
set "ICON_LINE="
if exist "graderise-icon.ico" (
    set "ICON_LINE=  ^<icon^>%CURRENT_DIR%\graderise-icon.ico^</icon^>"
    echo ℹ️  Using icon: graderise-icon.ico
) else (
    echo ℹ️  No icon file found (will use default)
)

REM Create updated Launch4j config
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<launch4jConfig^>
echo   ^<dontWrapJar^>false^</dontWrapJar^>
echo   ^<headerType^>gui^</headerType^>
echo   ^<jar^>%CURRENT_DIR%\GradeRise-Complete.jar^</jar^>
echo   ^<outfile^>%CURRENT_DIR%\GradeRise.exe^</outfile^>
echo   ^<errTitle^>GradeRise - Error^</errTitle^>
echo   ^<cmdLine^>^</cmdLine^>
echo   ^<chdir^>.^</chdir^>
echo   ^<priority^>normal^</priority^>
echo   ^<downloadUrl^>https://adoptium.net/^</downloadUrl^>
echo   ^<supportUrl^>https://github.com/KNGOFHUMANS/GPA-TRACKER^</supportUrl^>
echo   ^<stayAlive^>false^</stayAlive^>
echo   ^<restartOnCrash^>false^</restartOnCrash^>
echo   ^<manifest^>^</manifest^>
if defined ICON_LINE echo !ICON_LINE!
echo   ^<singleInstance^>
echo     ^<mutexName^>GradeRise^</mutexName^>
echo     ^<windowTitle^>GradeRise^</windowTitle^>
echo   ^</singleInstance^>
echo   ^<jre^>
echo     ^<path^>jre^</path^>
echo     ^<bundledJre64Bit^>true^</bundledJre64Bit^>
echo     ^<bundledJreAsFallback^>false^</bundledJreAsFallback^>
echo     ^<minVersion^>11^</minVersion^>
echo     ^<maxVersion^>^</maxVersion^>
echo     ^<jdkPreference^>preferJre^</jdkPreference^>
echo     ^<runtimeBits^>64/32^</runtimeBits^>
echo     ^<initialHeapSize^>256^</initialHeapSize^>
echo     ^<maxHeapSize^>1024^</maxHeapSize^>
echo   ^</jre^>
echo   ^<versionInfo^>
echo     ^<fileVersion^>1.0.0.0^</fileVersion^>
echo     ^<txtFileVersion^>1.0.0^</txtFileVersion^>
echo     ^<fileDescription^>GradeRise - College GPA Tracker^</fileDescription^>
echo     ^<copyright^>2025 GradeRise^</copyright^>
echo     ^<productVersion^>1.0.0.0^</productVersion^>
echo     ^<txtProductVersion^>1.0.0^</txtProductVersion^>
echo     ^<productName^>GradeRise^</productName^>
echo     ^<companyName^>GradeRise^</companyName^>
echo     ^<internalName^>graderise^</internalName^>
echo     ^<originalFilename^>GradeRise.exe^</originalFilename^>
echo     ^<trademarks^>^</trademarks^>
echo     ^<language^>ENGLISH_US^</language^>
echo   ^</versionInfo^>
echo ^</launch4jConfig^>
) > launch4j-standalone.xml

echo ✅ Configuration created

echo.
echo Step 3: Creating EXE with Launch4j...
%LAUNCH4J% launch4j-standalone.xml

if exist "GradeRise.exe" (
    echo ✅ EXE created successfully!
    echo.
    
    REM Get file size
    for %%A in (GradeRise.exe) do set size=%%~zA
    echo 📁 File: GradeRise.exe
    echo 📊 Size: !size! bytes
    echo.
    
    echo Step 4: Checking for bundled JRE...
    if exist "jre" (
        echo ✅ Bundled JRE found in 'jre' folder
        echo ℹ️  Users can run GradeRise.exe directly without Java installed
    ) else (
        echo ⚠️  No bundled JRE found
        echo.
        echo IMPORTANT: To make this fully standalone:
        echo 1. Download JRE 11+ from: https://adoptium.net/
        echo 2. Extract it to a 'jre' folder next to GradeRise.exe
        echo 3. The structure should be: jre\bin\java.exe
        echo.
        echo OR users will need Java 11+ installed on their system
    )
    
    echo.
    echo ========================================
    echo    ✅ EXE BUILD COMPLETE!
    echo ========================================
    echo.
    echo 📦 Created: GradeRise.exe
    echo.
    echo 🚀 To create a fully portable package:
    echo    1. Create a folder called "GradeRise-Portable"
    echo    2. Copy GradeRise.exe into it
    echo    3. Copy the 'data' folder into it (for database)
    echo    4. Copy the 'jre' folder into it (for bundled Java)
    echo    5. Copy graderise-icon.png (optional)
    echo    6. Zip the folder and distribute!
    echo.
    echo Users can then extract and run GradeRise.exe anywhere!
    echo.
) else (
    echo ❌ Failed to create EXE
    echo.
    echo Check for errors above or verify:
    echo - Launch4j is properly installed
    echo - GradeRise-Complete.jar exists
    echo - You have write permissions
    echo.
)

pause
