@echo off
echo Creating GradeRise 2.0 Fat JAR...

REM Clean up any existing build
if exist temp-jar rmdir /s /q temp-jar
mkdir temp-jar
cd temp-jar

REM Extract all dependency JARs
echo Extracting dependencies...
for %%f in (..\libs\*.jar) do (
    echo Extracting %%f
    "%JAVA_HOME%\bin\jar" xf "%%f"
)

REM Copy our compiled classes
echo Copying application classes...
copy ..\*.class . > nul

REM Copy data directory
echo Copying data directory...
xcopy ..\data data\ /E /I /Q > nul

REM Create the fat JAR
echo Creating fat JAR...
"%JAVA_HOME%\bin\jar" cfm ..\graderise2.0.jar ..\MANIFEST.MF .

cd ..
rmdir /s /q temp-jar

echo GradeRise 2.0 Fat JAR created successfully!
echo File: graderise2.0.jar