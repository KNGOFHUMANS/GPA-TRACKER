@echo off
title GradeRise 2.0 - College GPA Tracker
echo Starting GradeRise 2.0...
echo.

REM Try to run with system Java
java -jar graderise2.0.jar

REM If Java is not found in PATH, try common locations
if %errorlevel% neq 0 (
    echo Java not found in PATH. Trying common Java locations...
    
    REM Try Oracle Java
    if exist "C:\Program Files\Common Files\Oracle\Java\javapath\java.exe" (
        "C:\Program Files\Common Files\Oracle\Java\javapath\java.exe" -jar graderise2.0.jar
        goto :end
    )
    
    REM Try AdoptOpenJDK/Eclipse Temurin locations
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%i\bin\java.exe" (
            "%%i\bin\java.exe" -jar graderise2.0.jar
            goto :end
        )
    )
    
    REM Try OpenJDK locations
    for /d %%i in ("C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\java.exe" (
            "%%i\bin\java.exe" -jar graderise2.0.jar
            goto :end
        )
    )
    
    echo.
    echo ERROR: Java Runtime Environment not found!
    echo Please install Java 17 or later from: https://adoptium.net/download/
    echo.
    pause
)

:end