@echo off
echo ======================================
echo    GradeRise Portable JAR Builder
echo ======================================
echo.

:: Set variables
set APP_NAME=GradeRise-Portable
set MAIN_CLASS=CollegeGPATracker
set BUILD_DIR=build
set TEMP_DIR=temp_extract
set FINAL_JAR=%APP_NAME%.jar
set JAR_TOOL="C:\Program Files\Java\jdk-25\bin\jar.exe"
set JAVAC_TOOL="C:\Program Files\Java\jdk-25\bin\javac.exe"
set JAVA_TOOL="C:\Program Files\Java\jdk-25\bin\java.exe"

:: Clean previous builds
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
if exist %TEMP_DIR% rmdir /s /q %TEMP_DIR%
if exist %FINAL_JAR% del %FINAL_JAR%

:: Create build directories
mkdir %BUILD_DIR%
mkdir %TEMP_DIR%

echo [1/6] Compiling Java source files...
%JAVAC_TOOL% -cp ".;libs/*" -d %BUILD_DIR% *.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo ✓ Compilation successful

echo.
echo [2/6] Extracting dependency JARs...
pushd %TEMP_DIR%

:: Extract all dependency JARs
for %%f in (..\libs\*.jar) do (
    echo   Extracting %%f...
    %JAR_TOOL% xf "%%f"
    :: Remove signature files that can cause conflicts
    if exist META-INF\*.SF del META-INF\*.SF
    if exist META-INF\*.DSA del META-INF\*.DSA
    if exist META-INF\*.RSA del META-INF\*.RSA
)

popd
echo ✓ Dependencies extracted

echo.
echo [3/6] Copying compiled classes...
xcopy /s /q %BUILD_DIR%\* %TEMP_DIR%\
echo ✓ Classes copied

echo.
echo [4/6] Copying data directory and resources...
if exist data xcopy /s /q data %TEMP_DIR%\data\
if exist client_secret.json copy client_secret.json %TEMP_DIR%\
if exist graderise-icon.png copy graderise-icon.png %TEMP_DIR%\
echo ✓ Resources copied

echo.
echo [5/6] Creating MANIFEST.MF...
pushd %TEMP_DIR%
echo Manifest-Version: 1.0 > META-INF\MANIFEST.MF
echo Main-Class: %MAIN_CLASS% >> META-INF\MANIFEST.MF
echo Class-Path: . >> META-INF\MANIFEST.MF
echo Implementation-Title: GradeRise College GPA Tracker >> META-INF\MANIFEST.MF
echo Implementation-Version: 2.0 >> META-INF\MANIFEST.MF
echo Implementation-Vendor: GradeRise >> META-INF\MANIFEST.MF
echo Build-Date: %DATE% %TIME% >> META-INF\MANIFEST.MF
echo. >> META-INF\MANIFEST.MF
popd
echo ✓ Manifest created

echo.
echo [6/6] Building final JAR...
pushd %TEMP_DIR%
%JAR_TOOL% cfm ..\%FINAL_JAR% META-INF\MANIFEST.MF *
popd

:: Cleanup
rmdir /s /q %BUILD_DIR%
rmdir /s /q %TEMP_DIR%

if exist %FINAL_JAR% (
    echo.
    echo ======================================
    echo ✓ SUCCESS: Portable JAR created!
    echo ======================================
    echo File: %FINAL_JAR%
    echo Size: 
    dir %FINAL_JAR% | find "%FINAL_JAR%"
    echo.
    echo The JAR contains:
    echo • All compiled classes
    echo • All dependencies (Gson, Google APIs, Jakarta Mail, etc.)
    echo • Configuration files
    echo • Data directory structure
    echo • Complete runtime environment
    echo.
    echo Ready for conversion to EXE!
    echo ======================================
) else (
    echo ERROR: Failed to create JAR file!
    pause
    exit /b 1
)

echo.
echo Testing JAR file...
echo Running: %JAVA_TOOL% -jar %FINAL_JAR%
echo Press Ctrl+C to stop the test...
echo.
%JAVA_TOOL% -jar %FINAL_JAR%

pause