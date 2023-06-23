# @author Val Richter
# @reviewer Benjamin Frahm

javaPath=/lib/jvm/java-20-openjdk
javafxPath=/lib/jvm/java-20-openjdk/lib
jarName=SoundInvestments.jar
mavenJarPath=./target/sound-investments-1.0-SNAPSHOT-jar-with-dependencies.jar

# Build clean dist directory
if [ -d "./dist" ]; then
	rm -rf "./dist"
fi
mkdir "./dist"

# Build Fat-Jar
echo "Building JAR..."
mvn clean package >/dev/null
cp "$mavenJarPath" "./dist/$jarName" >/dev/null

# Build Java Runtime
echo "Building Runtime..."
cp -r "$javafxPath" "./dist/javafx" >/dev/null

/lib/jvm/java-20-openjdk/bin/jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path "$javaPath/jmods" --add-modules java.net.http,java.base,java.sql,jdk.localedata,java.desktop --module-path "./dist/javafx/lib" --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics --bind-services --output dist/java
cp -r ./dist/javafx/javafx* "./dist/java/bin" >/dev/null
rm -rf "./dist/javafx"

# Copy resources into the distributable
echo "Copying Resources..."
distResourcesPath=./dist/src/main
mkdir -p "$distResourcesPath"
cp -r "./src/main/resources" "$distResourcesPath" >/dev/null

# Write executable into the distributable
echo "Writing Script..."
echo "./java/bin/java --module-path javafx\lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics -jar $jarName" >dist/SoundInvestments.bash

echo "Done"