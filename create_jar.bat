@echo off
echo Creating GradeRise 2.0 JAR file...
jar cfm graderise2.0.jar MANIFEST.MF *.class
if exist graderise2.0.jar (
    echo Successfully created graderise2.0.jar
) else (
    echo Failed to create JAR file
)
pause