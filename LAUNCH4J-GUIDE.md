# Launch4j EXE Conversion Guide for GradeRise

## 🚀 Quick Setup Steps

### 1. Download and Install Launch4j
- Download Launch4j from: https://launch4j.sourceforge.net/
- Install it to a folder like `C:\Program Files (x86)\Launch4j`
- Launch the Launch4j application

### 2. Load Configuration
- In Launch4j, go to **File** → **Load config**
- Select the `launch4j-config.xml` file from your GradeRise folder
- This will automatically load all the correct settings

### 3. Verify Paths
Make sure these paths are correct in the configuration:

**Basic Tab:**
- **Output file**: `C:\Users\malik\OneDrive\Desktop\GPAManagerApp\GradeRise.exe`
- **Jar**: `C:\Users\malik\OneDrive\Desktop\GPAManagerApp\GradeRise-Complete.jar`
- **Icon**: `C:\Users\malik\OneDrive\Desktop\GPAManagerApp\graderise-icon.png`

**JRE Tab:**
- **Bundled JRE path**: `custom-jre`
- **Min JRE version**: `1.8.0`

### 4. Build the EXE
- Click the **Build wrapper** button (gear icon)
- Launch4j will create `GradeRise.exe` in your folder

## 🔧 Manual Configuration (if needed)

If you need to set up manually, here are the settings:

### Basic Tab
```
Output file: C:\Users\malik\OneDrive\Desktop\GPAManagerApp\GradeRise.exe
Jar: C:\Users\malik\OneDrive\Desktop\GPAManagerApp\GradeRise-Complete.jar
Icon: C:\Users\malik\OneDrive\Desktop\GPAManagerApp\graderise-icon.png
```

### Classpath Tab
```
Main class: CollegeGPATracker
Classpath: libs/*
```

### JRE Tab
```
Bundled JRE path: custom-jre
Min JRE version: 1.8.0
Max JRE version: (leave empty)
```

### Version Info Tab
```
File version: 1.0.0.0
Product version: 1.0.0.0
File description: GradeRise - Rise Above Average
Copyright: 2025 GradeRise
Product name: GradeRise
Company name: GradeRise
Original filename: GradeRise.exe
Internal name: GradeRise
```

## 🐛 Troubleshooting

### If the EXE doesn't start:

1. **Check the debug log**: Look for `graderise-debug.log` in the same folder as the EXE
2. **Test the JAR first**: Run `java -jar GradeRise-Complete.jar` to make sure it works
3. **Verify JRE path**: Make sure the `custom-jre` folder is in the same location as the EXE
4. **Run from command line**: Open CMD and run `GradeRise.exe` to see error messages

### Common Issues:

**"Could not find main class"**
- Make sure the JAR file path is correct
- Verify the main class is set to `CollegeGPATracker`

**"JRE not found"**
- Check that `custom-jre` folder exists
- Ensure the bundled JRE path is set to `custom-jre` (not an absolute path)

**"Icon not found"**
- Verify `graderise-icon.png` exists in the project folder
- You can skip the icon if needed by leaving the Icon field empty

## 📋 File Structure After Build
```
GPAManagerApp/
├── GradeRise.exe                ← Your new EXE file
├── GradeRise-Complete.jar       ← Original JAR
├── graderise-icon.png           ← Icon file
├── launch4j-config.xml          ← Configuration file
├── custom-jre/                  ← Bundled Java runtime
├── libs/                        ← Library dependencies
├── data/                        ← Application data
└── graderise-debug.log          ← Debug log (created when EXE runs)
```

## ✅ Testing Your EXE

1. **Double-click** `GradeRise.exe` to launch
2. **Check the log**: If it doesn't start, read `graderise-debug.log`
3. **Verify icon**: The EXE should show the GradeRise graduation cap icon
4. **Test functionality**: Make sure all features work the same as the JAR version

## 💡 Tips for Distribution

- **Include everything**: When sharing your app, include the EXE and `custom-jre` folder
- **Test on other computers**: The EXE should work without requiring Java installation
- **Keep files together**: Don't separate the EXE from the `custom-jre` folder

## 🆘 If You Still Have Issues

The enhanced error handling and debug logging should help identify the problem. The log file will show:

- Java version and system info
- Working directory and classpath
- Step-by-step startup progress
- Detailed error messages if something fails

Check the `graderise-debug.log` file for specific error details, and the application will also show user-friendly error dialogs with troubleshooting guidance.