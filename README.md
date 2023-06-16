# SoundInvestments

This is the source code for the "SoundInvestments" project from the lecture SoftwareEngineering II. \
The application aims to support stock analysts by sonifying trade data and thereby enabling a more intuitive understanding of market trends.

## How to build the Distributable

1. Make sure you have `maven` and `jlink` installed and in your path. To test this, you can run `mvn --version` and `jlink --version`, respectively
2. Go into `build.bat` (or `build.bash` if you're not on windows) and set the java path and javafx path variables at the top of the script. These variables should be the paths pointing at your local `java` and `javafx` isntallations.
3. Run the script (`build.bat` on windows and `build.bash` on linux/mac)
4. You can now go into the newly created `dist/` folder and run the application via the `SoundInvestments.bat` or `SounInvestments.bash` script

## Developers
- [Rex2002](https://github.com/Rex2002) - Music and synthesizer
- [ArtInLines](https://github.com/ArtInLines) - Controller and Data flow
- [MalteRichert](https://github.com/MalteRichert) - Music and harmonizer
- [Lizzyhara](https://github.com/Lizzyhara) - User Interface
- [JakobPK](https://github.com/JakobPK) - Data analysis
- [nichtLehdev](https://github.com/nichtLehdev) - Music and mixer
