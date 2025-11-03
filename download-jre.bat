@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    JRE Downloader for GradeRise
echo ========================================
echo.
echo This script will download a portable JRE
echo for bundling with GradeRise.exe
echo.

REM Check if jre folder already exists
if exist "jre" (
    echo WARNING: 'jre' folder already exists!
    echo.
    choice /C YN /M "Do you want to delete it and download a fresh JRE"
    if errorlevel 2 (
        echo Cancelled. Using existing JRE.
        pause
        exit /b 0
    )
    echo Deleting existing jre folder...
    rmdir /s /q jre
)

echo.
echo Downloading JRE from Adoptium (Eclipse Temurin)...
echo This may take a few minutes depending on your connection.
echo.

REM Download JRE 17 (LTS) - Windows x64
set JRE_URL=https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse
set JRE_ZIP=jre-download.zip

echo 📥 Downloading JRE 17 (x64)...
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '%JRE_URL%' -OutFile '%JRE_ZIP%' -UseBasicParsing }"

if not exist "%JRE_ZIP%" (
    echo ❌ Download failed!
    echo.
    echo Please manually download JRE from:
    echo https://adoptium.net/temurin/releases/
    echo.
    echo Select: Version 17, Windows x64, JRE package type ZIP
    pause
    exit /b 1
)

echo ✅ Download complete!
echo.
echo 📦 Extracting JRE...
powershell -Command "Expand-Archive -Path '%JRE_ZIP%' -DestinationPath '.' -Force"

echo.
echo 🔧 Renaming folder to 'jre'...
for /d %%i in (jdk-*-jre) do (
    ren "%%i" jre
    echo ✅ Renamed %%i to jre
)

echo.
echo 🧹 Cleaning up...
del "%JRE_ZIP%"

if exist "jre\bin\java.exe" (
    echo.
    echo ========================================
    echo    ✅ JRE SETUP COMPLETE!
    echo ========================================
    echo.
    echo 📁 JRE Location: %CD%\jre
    echo.
    
    REM Get JRE size
    powershell -Command "& { $size = (Get-ChildItem -Path 'jre' -Recurse | Measure-Object -Property Length -Sum).Sum; Write-Host '📊 JRE Size:' ([math]::Round($size/1MB, 2)) 'MB' }"
    
    echo.
    echo ✅ Your GradeRise.exe can now run without Java installed!
    echo.
    echo Next steps:
    echo 1. Test GradeRise.exe to ensure it works
    echo 2. Create portable package (see STANDALONE-EXE-GUIDE.md)
    echo 3. Distribute to users!
    echo.
) else (
    echo ❌ JRE setup failed!
    echo The java.exe file was not found in the expected location.
    echo.
    echo Please manually download and extract JRE to 'jre' folder.
    pause
    exit /b 1
)

pause
