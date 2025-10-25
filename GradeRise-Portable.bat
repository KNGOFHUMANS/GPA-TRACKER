@echo off
REM GradeRise Portable Launcher
REM This batch file allows GradeRise.exe to run from any location

REM Get the directory where this batch file is located
set "BATCH_DIR=%~dp0"

REM Change to the GradeRise application directory
cd /d "%BATCH_DIR%"

REM Launch GradeRise.exe
"%BATCH_DIR%GradeRise.exe"

REM Return to the original directory if needed
cd /d "%OLDDIR%"