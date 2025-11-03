@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   GradeRise Portable EXE Builder
echo ========================================
echo.
echo This creates an EXE that works ANYWHERE:
echo - Uses system Java if available
echo - Falls back to bundled JRE if present
echo - Shows download link if neither found
echo.

REM Check Launch4j
if not exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    if not exist "C:\Program Files\Launch4j\launch4jc.exe" (
        echo ERROR: Launch4j not found!
        pause
        exit /b 1
    )
    set LAUNCH4J="C:\Program Files\Launch4j\launch4jc.exe"
) else (
    set LAUNCH4J="C:\Program Files (x86)\Launch4j\launch4jc.exe"
)

set "CURRENT_DIR=%CD%"

REM Create portable config
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<launch4jConfig^>
echo   ^<dontWrapJar^>false^</dontWrapJar^>
echo   ^<headerType^>gui^</headerType^>
echo   ^<jar^>%CURRENT_DIR%\GradeRise-Complete.jar^</jar^>
echo   ^<outfile^>%CURRENT_DIR%\GradeRise-Portable.exe^</outfile^>
echo   ^<errTitle^>GradeRise^</errTitle^>
echo   ^<cmdLine^>^</cmdLine^>
echo   ^<chdir^>.^</chdir^>
echo   ^<priority^>normal^</priority^>
echo   ^<downloadUrl^>https://adoptium.net/^</downloadUrl^>
echo   ^<supportUrl^>^</supportUrl^>
echo   ^<stayAlive^>false^</stayAlive^>
echo   ^<restartOnCrash^>false^</restartOnCrash^>
echo   ^<manifest^>^</manifest^>
echo   ^<jre^>
echo     ^<path^>^</path^>
echo     ^<bundledJre64Bit^>false^</bundledJre64Bit^>
echo     ^<bundledJreAsFallback^>true^</bundledJreAsFallback^>
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
echo     ^<originalFilename^>GradeRise-Portable.exe^</originalFilename^>
echo     ^<language^>ENGLISH_US^</language^>
echo   ^</versionInfo^>
echo ^</launch4jConfig^>
) > launch4j-system-java.xml

echo ✅ Configuration created
echo.
echo Building portable EXE...
%LAUNCH4J% launch4j-system-java.xml

if exist "GradeRise-Portable.exe" (
    echo.
    echo ========================================
    echo   ✅ SUCCESS!
    echo ========================================
    echo.
    for %%A in (GradeRise-Portable.exe) do (
        echo 📁 File: GradeRise-Portable.exe
        echo 📊 Size: %%~zA bytes
    )
    echo.
    echo ✅ This EXE will work if users have Java 11+ installed
    echo.
    echo To test:
    echo 1. Copy GradeRise-Portable.exe to Desktop
    echo 2. Double-click to run
    echo.
    echo If user doesn't have Java, they'll see download link
    echo.
) else (
    echo ❌ Build failed!
)

pause
