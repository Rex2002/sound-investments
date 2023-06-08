# @author Val Richter

javaPath=C:/Languages/Java/jdk-20.0.1
javafxPath=C:/Languages/Java/javafx-sdk-20.0.1
jarName=SoundInvestments.jar
mavenJarPath=./target/sound-investments-1.0-SNAPSHOT-jar-with-dependencies.jar

# Build clean dist directory
if [ -d "./dist" ]; then
	rm -rf "./dist"
fi
mkdir "./dist"

# Build Fat-Jar
echo "Building JAR..."
# mvn clean package >/dev/null
cp "$mavenJarPath" "./dist/$jarName" >/dev/null

# Build Java Runtime
echo "Building Runtime..."
cp -r "$javafxPath" "./dist/javafx" >/dev/null
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path "$javaPath/jmods" --add-modules java.net.http,java.base,java.sql,jdk.localedata,java.desktop --module-path "./dist/javafx/lib" --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics --bind-services --output dist/java
cp -r "./dist/javafx/bin" "./dist/java/bin" >/dev/null
rm -rf "./dist/javafx/bin"     >/dev/null
rm -rf "./dist/javafx/legal"   >/dev/null
rm     "./dist/javafx/src.zip" >/dev/null

# Copy resources into the distributable
echo "Copying Resources..."
distResourcesPath=./dist/src/main/resources
mkdir -p "$distResourcesPath"
cp -r "./src/main/resources" "$distResourcesPath" >/dev/null

# Write executable into the distributable
echo "Writing Script..."
echo "java\bin\java.exe --module-path javafx\lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics -jar $jarName" >dist/SoundInvestments.bat
echo "./java/bin/java.exe --module-path javafx\lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics -jar $jarName" >dist/SoundInvestments.bash

echo "Done"