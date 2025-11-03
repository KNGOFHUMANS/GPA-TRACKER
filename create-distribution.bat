@echo off
echo ========================================
echo   Creating GradeRise Distribution
echo ========================================
echo.

REM Create distribution folder
if exist "GradeRise-Portable" rmdir /s /q "GradeRise-Portable"
mkdir "GradeRise-Portable"
echo Created GradeRise-Portable folder

REM Copy EXE
echo Copying GradeRise.exe...
copy "GradeRise.exe" "GradeRise-Portable\" >nul

REM Copy JRE
echo Copying JRE folder (this takes a minute)...
xcopy "jre" "GradeRise-Portable\jre\" /E /I /H /Y >nul

REM Create README
echo Creating README.txt...
(
echo GradeRise - College GPA Tracker
echo ================================
echo.
echo QUICK START:
echo 1. Double-click GradeRise.exe
echo 2. Create account or sign in with Google
echo 3. Start tracking your grades!
echo.
echo FEATURES:
echo - Track courses, assignments, and GPA
echo - Google OAuth integration
echo - 4 beautiful themes
echo - Data visualization
echo - What-if scenario planning
echo.
echo REQUIREMENTS:
echo - Windows 7 or later
echo - No Java installation needed ^(bundled^)
echo.
echo PORTABLE:
echo - Run from anywhere
echo - Copy to USB drive
echo - Move between computers
echo - Data stays in 'data' folder
echo.
echo SIZE: ~145 MB total
echo VERSION: 1.0.0
echo.
echo GitHub: https://github.com/KNGOFHUMANS/GPA-TRACKER
) > "GradeRise-Portable\README.txt"

echo.
echo ========================================
echo   SUCCESS!
echo ========================================
echo.
echo Created: GradeRise-Portable folder
echo.
dir "GradeRise-Portable"
echo.
echo Ready to distribute!
echo.
pause
