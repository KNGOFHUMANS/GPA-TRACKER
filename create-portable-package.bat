@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   GradeRise Portable Package Creator
echo ========================================
echo.

REM Check if EXE exists
if not exist "GradeRise.exe" (
    echo ❌ ERROR: GradeRise.exe not found!
    echo Please run create-standalone-exe.bat first.
    pause
    exit /b 1
)

REM Check if JRE exists
if not exist "jre\bin\java.exe" (
    echo ⚠️  WARNING: Bundled JRE not found!
    echo.
    choice /C YN /M "Do you want to download JRE now (recommended)"
    if errorlevel 2 (
        echo.
        echo ⚠️  Continuing without JRE...
        echo Users will need Java installed on their system.
        echo.
        set NO_JRE=1
    ) else (
        call download-jre.bat
        if errorlevel 1 (
            echo JRE download failed. Continuing without JRE...
            set NO_JRE=1
        )
    )
)

echo.
echo Creating portable package...
echo.

REM Create distribution folder
set DIST_FOLDER=GradeRise-Portable
if exist "%DIST_FOLDER%" (
    echo Cleaning old portable folder...
    rmdir /s /q "%DIST_FOLDER%"
)

mkdir "%DIST_FOLDER%"
echo ✅ Created folder: %DIST_FOLDER%

REM Copy EXE
echo 📦 Copying GradeRise.exe...
copy "GradeRise.exe" "%DIST_FOLDER%\" >nul
if errorlevel 1 (
    echo ❌ Failed to copy EXE
    pause
    exit /b 1
)

REM Copy JRE if it exists
if not defined NO_JRE (
    if exist "jre" (
        echo 📦 Copying JRE (this may take a moment)...
        xcopy "jre" "%DIST_FOLDER%\jre\" /E /I /H /Y >nul
        if errorlevel 1 (
            echo ⚠️  Warning: JRE copy incomplete
        ) else (
            echo ✅ JRE copied successfully
        )
    )
)

REM Copy data folder if it exists
if exist "data" (
    echo 📦 Copying data folder...
    xcopy "data" "%DIST_FOLDER%\data\" /E /I /H /Y >nul
    echo ✅ Data folder copied
) else (
    echo ℹ️  No data folder found (will be created on first run)
)

REM Create README
echo 📄 Creating README.txt...
(
echo GradeRise - College GPA Tracker
echo ================================
echo.
echo QUICK START:
echo 1. Double-click GradeRise.exe to launch
echo 2. Create an account or sign in with Google
echo 3. Start tracking your grades!
echo.
echo FEATURES:
echo - Track courses, assignments, and GPA
echo - Google OAuth integration
echo - Beautiful modern themes ^(Lavender, Cream, Crimson Noir, Teal^)
echo - Data visualization ^& analytics
echo - What-if scenario planning
echo - Grade export functionality
echo.
echo REQUIREMENTS:
if defined NO_JRE (
echo - Windows 7 or later
echo - Java 11 or later REQUIRED ^(download from https://adoptium.net/^)
) else (
echo - Windows 7 or later
echo - No Java installation needed ^(bundled with JRE 17^)
)
echo - 100 MB free disk space
echo.
echo PORTABLE:
echo This is a portable application. You can:
echo - Run it from any folder
echo - Copy it to a USB drive
echo - Move it between computers
echo - Your data stays in the 'data' folder
echo.
echo DATA LOCATION:
echo All your data is stored in the 'data' folder:
echo - users.json: User accounts
echo - user_data.json: Courses, assignments, grades
echo - session.json: Login sessions
echo - *.json: Various settings and preferences
echo.
echo THEMES:
echo GradeRise includes 4 beautiful color schemes:
echo 1. Lavender ^& Red: Elegant purple with rose accents
echo 2. Cream ^& Amber: Warm beige with golden highlights
echo 3. Crimson Noir: Dark burgundy for late-night studying
echo 4. Teal ^& Pink: Fresh aqua with vibrant accents
echo.
echo Change themes anytime from the dashboard!
echo.
echo SUPPORT ^& UPDATES:
echo - GitHub: https://github.com/KNGOFHUMANS/GPA-TRACKER
echo - Issues: Report bugs or request features on GitHub
echo - Version: 1.0.0
echo.
echo BACKUP YOUR DATA:
echo Your grades are important! Regularly backup the 'data' folder
echo to prevent data loss.
echo.
echo LICENSE:
echo Copyright 2025 GradeRise
echo.
echo Thank you for using GradeRise! Happy studying! 📚
) > "%DIST_FOLDER%\README.txt"

echo ✅ README.txt created

REM Get folder size
echo.
echo 📊 Calculating package size...
powershell -Command "& { $size = (Get-ChildItem -Path '%DIST_FOLDER%' -Recurse | Measure-Object -Property Length -Sum).Sum; Write-Host 'Package Size:' ([math]::Round($size/1MB, 2)) 'MB' }"

echo.
echo ========================================
echo    ✅ PORTABLE PACKAGE CREATED!
echo ========================================
echo.
echo 📁 Location: %CD%\%DIST_FOLDER%
echo.
echo Contents:
dir /B "%DIST_FOLDER%"
echo.

REM Ask if user wants to create ZIP
echo.
choice /C YN /M "Do you want to create a ZIP archive for distribution"
if errorlevel 2 goto :skip_zip

echo.
echo Creating ZIP archive...
set ZIP_NAME=GradeRise-v1.0-Portable.zip
if exist "%ZIP_NAME%" del "%ZIP_NAME%"

powershell -Command "Compress-Archive -Path '%DIST_FOLDER%\*' -DestinationPath '%ZIP_NAME%' -CompressionLevel Optimal"

if exist "%ZIP_NAME%" (
    echo ✅ ZIP created: %ZIP_NAME%
    
    REM Get ZIP size
    for %%A in ("%ZIP_NAME%") do (
        set size=%%~zA
        set /a sizeMB=!size! / 1048576
        echo 📊 ZIP Size: !sizeMB! MB
    )
) else (
    echo ❌ Failed to create ZIP
)

:skip_zip

echo.
echo ========================================
echo    📦 DISTRIBUTION READY!
echo ========================================
echo.
if defined NO_JRE (
    echo ⚠️  IMPORTANT: This package requires Java 11+
    echo Users must have Java installed to run GradeRise.exe
    echo.
    echo To create a fully standalone package:
    echo 1. Run download-jre.bat to get a bundled JRE
    echo 2. Run this script again
    echo.
) else (
    echo ✅ This is a fully standalone package!
    echo Users do NOT need Java installed.
    echo.
)
echo 🚀 You can now distribute:
if exist "%ZIP_NAME%" (
    echo    - %ZIP_NAME% ^(upload to GitHub, Google Drive, etc.^)
) else (
    echo    - %DIST_FOLDER% folder ^(compress or share directly^)
)
echo.
echo 📝 See STANDALONE-EXE-GUIDE.md for detailed distribution instructions
echo.
echo Test the package on a clean machine before distributing!
echo.

pause
