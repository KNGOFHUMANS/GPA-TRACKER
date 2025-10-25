# 🚀 Making GradeRise Truly Portable

## The Issue
Launch4j creates an EXE that still depends on the surrounding files (libs, custom-jre, data) being in the same directory. To make it truly portable, you need to distribute the entire folder structure.

## 📦 Portable Distribution Package

To make GradeRise work from any location, you need to distribute these files together:

```
GradeRise-Portable/
├── GradeRise.exe          ← The main executable
├── CollegeGPAApp.jar      ← Your application
├── custom-jre/            ← Bundled Java runtime (entire folder)
├── libs/                  ← All dependencies (entire folder)
├── data/                  ← Application data (entire folder)
└── tokens/                ← OAuth tokens (if exists)
```

## ✅ How to Create Portable Distribution

1. **Create a distribution folder**:
   ```bash
   mkdir GradeRise-Portable
   ```

2. **Copy all necessary files**:
   - Copy `GradeRise.exe`
   - Copy `CollegeGPAApp.jar`  
   - Copy entire `custom-jre` folder
   - Copy entire `libs` folder
   - Copy entire `data` folder
   - Copy `tokens` folder (if it exists)

3. **Test the portable version**:
   - Move the `GradeRise-Portable` folder anywhere
   - Double-click `GradeRise.exe` inside the folder
   - It should work perfectly!

## 🎯 Why This Works

The Launch4j configuration now uses `%EXEDIR%` which means:
- The EXE looks for files relative to its own location
- As long as all files stay in the same relative structure, it will work
- Users can put the folder anywhere and it will run

## 📋 Distribution Instructions for Users

Tell your users:
1. Download the `GradeRise-Portable` folder
2. Keep all files together in the same folder
3. Double-click `GradeRise.exe` to run
4. No Java installation required!

## 💡 Pro Tip
You can zip the entire `GradeRise-Portable` folder for easy distribution. Users just need to:
1. Download and unzip
2. Run `GradeRise.exe`
3. Enjoy GradeRise! 🎓