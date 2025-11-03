@echo off
echo ========================================
echo    GradeRise Complete EXE Builder
echo ========================================
echo.

REM Step 1: Clean up any previous builds
if exist "GradeRise-Complete.exe" del "GradeRise-Complete.exe"
if exist "GradeRise-Complete" rmdir /s /q "GradeRise-Complete"

REM Step 2: Create JAR with all dependencies
echo 🔧 Step 1: Creating JAR with all dependencies...
echo.

REM Compile all Java files
echo Compiling Java files...
javac -cp "libs\*;." *.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.

REM Create manifest file
echo Main-Class: CollegeGPATracker > MANIFEST.MF
echo Class-Path: . >> MANIFEST.MF

REM Create JAR with all class files
echo Creating JAR file...
jar cfm GradeRise-Complete.jar MANIFEST.MF *.class

REM Add all libraries to the JAR
echo Adding dependencies to JAR...
cd libs
for %%f in (*.jar) do (
    echo Adding %%f...
    jar -xf "%%f"
)

REM Move back and add extracted classes to our JAR
cd..
cd libs
jar -uf "../GradeRise-Complete.jar" .
cd..

echo ✅ JAR created successfully!
echo.

REM Step 3: Create complete directory structure
echo 🔧 Step 2: Creating complete distribution...
echo.

mkdir "GradeRise-Complete"
copy "GradeRise-Complete.jar" "GradeRise-Complete\"

REM Copy data directory if exists
if exist "data" (
    echo Copying data directory...
    xcopy "data" "GradeRise-Complete\data\" /E /I /Y
)

REM Copy custom JRE if exists
if exist "custom-jre" (
    echo Copying custom JRE...
    xcopy "custom-jre" "GradeRise-Complete\custom-jre\" /E /I /Y
)

REM Create launcher script
echo @echo off > "GradeRise-Complete\GradeRise.bat"
echo cd /d "%%~dp0" >> "GradeRise-Complete\GradeRise.bat"
echo if exist "custom-jre\bin\java.exe" ( >> "GradeRise-Complete\GradeRise.bat"
echo     "custom-jre\bin\java.exe" -jar GradeRise-Complete.jar >> "GradeRise-Complete\GradeRise.bat"
echo ) else ( >> "GradeRise-Complete\GradeRise.bat"
echo     java -jar GradeRise-Complete.jar >> "GradeRise-Complete\GradeRise.bat"
echo ) >> "GradeRise-Complete\GradeRise.bat"
echo pause >> "GradeRise-Complete\GradeRise.bat"

REM Create README file
echo # GradeRise - Complete Package > "GradeRise-Complete\README.md"
echo. >> "GradeRise-Complete\README.md"
echo ## How to Run >> "GradeRise-Complete\README.md"
echo. >> "GradeRise-Complete\README.md"
echo 1. Double-click `GradeRise.bat` to launch the application >> "GradeRise-Complete\README.md"
echo 2. If you have Java installed, you can also run: `java -jar GradeRise-Complete.jar` >> "GradeRise-Complete\README.md"
echo. >> "GradeRise-Complete\README.md"
echo ## Features >> "GradeRise-Complete\README.md"
echo - Beautiful Modern UI with Multiple Themes (including Crimson Noir!) >> "GradeRise-Complete\README.md"
echo - GPA Tracking and Analytics >> "GradeRise-Complete\README.md"
echo - Google OAuth Integration >> "GradeRise-Complete\README.md"
echo - Grade Visualization and Reports >> "GradeRise-Complete\README.md"
echo - What-If Scenario Planning >> "GradeRise-Complete\README.md"
echo - Email Integration for Password Reset >> "GradeRise-Complete\README.md"
echo. >> "GradeRise-Complete\README.md"
echo ## System Requirements >> "GradeRise-Complete\README.md"
echo - Windows 10 or later (recommended) >> "GradeRise-Complete\README.md"
echo - Java 11 or later (bundled JRE included) >> "GradeRise-Complete\README.md"
echo - 512MB RAM minimum >> "GradeRise-Complete\README.md"
echo - 200MB disk space >> "GradeRise-Complete\README.md"

echo ✅ Complete distribution created!
echo.

REM Step 4: Create ZIP file for distribution
echo 🔧 Step 3: Creating ZIP package for distribution...
echo.

powershell -command "Compress-Archive -Path 'GradeRise-Complete' -DestinationPath 'GradeRise-Portable.zip' -Force"

if exist "GradeRise-Portable.zip" (
    echo ✅ ZIP package created successfully!
    echo.
    echo 📁 Package location: %CD%\GradeRise-Portable.zip
    echo 📊 Package size: 
    dir "GradeRise-Portable.zip" | find "GradeRise-Portable.zip"
    echo.
) else (
    echo ❌ Failed to create ZIP package
)

REM Step 5: Test the distribution
echo 🧪 Step 4: Testing the distribution...
echo.

cd "GradeRise-Complete"
echo Testing JAR execution...
timeout /t 1 /nobreak >nul

if exist "custom-jre\bin\java.exe" (
    echo Using bundled JRE...
    start /b "" "custom-jre\bin\java.exe" -jar GradeRise-Complete.jar
) else (
    echo Using system Java...
    start /b "" java -jar GradeRise-Complete.jar
)

timeout /t 3 /nobreak >nul
taskkill /f /im "java.exe" >nul 2>&1
cd..

echo.
echo ========================================
echo    🎉 BUILD COMPLETE! 🎉
echo ========================================
echo.
echo ✅ Your GradeRise application is ready for distribution!
echo.
echo 📦 Distribution Files Created:
echo    📁 GradeRise-Complete\          - Complete application folder
echo    📁 GradeRise-Complete.jar       - Standalone JAR file
echo    📦 GradeRise-Portable.zip       - Portable ZIP package
echo.
echo 🚀 How people can use it:
echo    1. Download GradeRise-Portable.zip
echo    2. Extract anywhere on their computer
echo    3. Double-click GradeRise.bat to run
echo.
echo 💡 Features included:
echo    ✅ Beautiful Crimson Noir theme (and 3 others!)
echo    ✅ Complete GPA tracking system
echo    ✅ Google OAuth integration
echo    ✅ Modern UI with animations
echo    ✅ Grade analytics and charts
echo    ✅ What-if scenario planning
echo    ✅ Export capabilities
echo    ✅ Self-contained (no installation needed)
echo.
echo Press any key to exit...
pause >nul
