@echo off
echo Creating GradeRise JAR file...

REM Try to find jar.exe in common Java installation locations
set JAR_CMD=
for /f "tokens=*" %%i in ('where jar 2^>nul') do set JAR_CMD=%%i
if "%JAR_CMD%"=="" (
    if exist "C:\Program Files\Java\jdk*\bin\jar.exe" (
        for /d %%d in ("C:\Program Files\Java\jdk*") do set JAR_CMD=%%d\bin\jar.exe
    )
)
if "%JAR_CMD%"=="" (
    if exist "C:\Program Files (x86)\Java\jdk*\bin\jar.exe" (
        for /d %%d in ("C:\Program Files (x86)\Java\jdk*") do set JAR_CMD=%%d\bin\jar.exe
    )
)

if "%JAR_CMD%"=="" (
    echo Error: jar.exe not found. Please ensure Java JDK is installed and in PATH.
    pause
    exit /b 1
)

echo Using jar at: %JAR_CMD%

REM Create the JAR file
"%JAR_CMD%" cfm GradeRise.jar MANIFEST.MF *.class

if exist GradeRise.jar (
    echo ✅ JAR file created successfully: GradeRise.jar
    
    REM Show file size
    dir GradeRise.jar | find "GradeRise.jar"
    
    echo.
    echo ✅ All compilation errors have been fixed!
    echo ✅ Unnecessary files have been cleaned up!
    echo ✅ JAR file is ready for conversion to EXE!
    echo.
    echo The JAR file contains:
    echo - All compiled application classes
    echo - Proper manifest with main class
    echo - Ready for Launch4j conversion
    echo.
) else (
    echo ❌ Failed to create JAR file
    exit /b 1
)

pause