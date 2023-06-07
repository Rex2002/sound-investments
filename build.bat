@REM @author Val Richter

@echo off
set java-path="C:\Languages\Java\jdk-20.0.1"
set javafx-path="C:\Languages\Java\javafx-sdk-20.0.1"

set "jar-name=SoundInvestments.jar"
set "robocopy-params=/E /njh /njs /ndl /nc /ns >NUL"


@REM Build clean dist directory
if exist dist (	@RD /S /Q dist )
mkdir dist

@REM Build Fat-Jar
echo Building JAR...
call mvn clean package >NUL
copy target\sound-investments-1.0-SNAPSHOT-jar-with-dependencies.jar dist\%jar-name% >NUL

@REM Build Java Runtime
echo Building Runtime...
robocopy %javafx-path% dist\javafx %robocopy-params%
call jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path %java-path%\jmods --add-modules java.net.http,java.base,java.sql,jdk.localedata,java.desktop --module-path dist\javafx\lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics --bind-services --output dist\java
robocopy dist\javafx\bin dist\java\bin %robocopy-params%
@RD /S /Q dist\javafx\bin
@RD /S /Q dist\javafx\legal
@RD /S /Q dist\javafx\src.zip

@REM Copy resources into the distributable
echo Copying Resources...
robocopy src\main\resources dist\src\main\resources %robocopy-params%
copy DistReadme.txt dist\README.txt >NUL

@REM Write executable into the distributable
echo Writing Batch-Script...
echo java\bin\java.exe --module-path javafx\lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics -jar %jar-name% > dist/SoundInvestments.bat

echo Done