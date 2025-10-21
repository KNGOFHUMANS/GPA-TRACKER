@echo off
title GPA Tracker Installer v2.1
color 0A

echo.
echo  ╔═══════════════════════════════════════════════════════╗
echo  ║                 GPA TRACKER v2.1                      ║
echo  ║              Professional Installer                   ║
echo  ╚═══════════════════════════════════════════════════════╝
echo.

echo [INFO] Installing GPA Tracker to your system...
echo [INFO] This will create a desktop shortcut and start menu entry

set "INSTALL_DIR=%USERPROFILE%\AppData\Local\GPATracker"
set "DESKTOP=%USERPROFILE%\Desktop"
set "START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs"

echo [1/4] Creating installation directory...
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
if not exist "%INSTALL_DIR%\libs" mkdir "%INSTALL_DIR%\libs"

echo [2/4] Copying application files...
copy "GPATracker-v2.0.jar" "%INSTALL_DIR%\" > nul
copy "app-icon*.png" "%INSTALL_DIR%\" > nul
copy "app-icon.ico" "%INSTALL_DIR%\" > nul
copy "client_secret.json.template" "%INSTALL_DIR%\" > nul
xcopy "libs" "%INSTALL_DIR%\libs\" /E /Q > nul

echo [3/4] Creating shortcuts...
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%TEMP%\CreateShortcut.vbs"
echo sLinkFile = "%DESKTOP%\GPA Tracker.lnk" >> "%TEMP%\CreateShortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\CreateShortcut.vbs"
echo oLink.TargetPath = "java" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Arguments = "-cp ""%INSTALL_DIR%\GPATracker-v2.0.jar;%INSTALL_DIR%\libs\*"" CollegeGPATracker" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.IconLocation = "%INSTALL_DIR%\app-icon.ico" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Description = "GPA Tracker - Academic Success Companion" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Save >> "%TEMP%\CreateShortcut.vbs"
cscript "%TEMP%\CreateShortcut.vbs" /nologo > nul

echo sLinkFile = "%START_MENU%\GPA Tracker.lnk" > "%TEMP%\CreateShortcut2.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.TargetPath = "java" >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.Arguments = "-cp ""%INSTALL_DIR%\GPATracker-v2.0.jar;%INSTALL_DIR%\libs\*"" CollegeGPATracker" >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.IconLocation = "%INSTALL_DIR%\app-icon.ico" >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.Description = "GPA Tracker - Academic Success Companion" >> "%TEMP%\CreateShortcut2.vbs"
echo oLink.Save >> "%TEMP%\CreateShortcut2.vbs"
cscript "%TEMP%\CreateShortcut2.vbs" /nologo > nul

echo [4/4] Finalizing installation...
del "%TEMP%\CreateShortcut.vbs" > nul 2>&1
del "%TEMP%\CreateShortcut2.vbs" > nul 2>&1

echo.
echo ✅ Installation Complete!
echo.
echo 🎓 GPA Tracker has been installed successfully!
echo 📍 Installation location: %INSTALL_DIR%
echo 🖥️  Desktop shortcut: Created
echo 📋 Start Menu entry: Created
echo 🎨 Custom icon: Red graduation cap
echo.
echo You can now:
echo • Double-click "GPA Tracker" on your desktop
echo • Find it in your Start Menu
echo • Pin it to taskbar for easy access
echo.
echo Press any key to launch GPA Tracker now...
pause > nul

echo Starting GPA Tracker...
cd /d "%INSTALL_DIR%"
start "GPA Tracker" java -cp "GPATracker-v2.0.jar;libs/*" CollegeGPATracker