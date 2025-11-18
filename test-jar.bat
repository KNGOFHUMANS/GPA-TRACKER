@echo off
echo ======================================
echo    GradeRise Portable JAR Test
echo ======================================
echo.
echo Testing JAR file: GradeRise-Portable.jar
echo.

if not exist GradeRise-Portable.jar (
    echo ERROR: GradeRise-Portable.jar not found!
    echo Please run create-portable-complete.bat first.
    pause
    exit /b 1
)

echo JAR file found. Size:
dir GradeRise-Portable.jar | find "GradeRise-Portable.jar"
echo.

echo Testing JAR execution...
echo (This will launch the application - close it to continue)
echo.
"C:\Program Files\Java\jdk-25\bin\java.exe" -jar "GradeRise-Portable.jar"

echo.
echo JAR test completed!
pause