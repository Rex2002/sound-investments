package audio.mixer;

import app.AppError;
import audio.Constants;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Backing {
    private final int SAMPLE_BARS = 4;
    private double[] groove;
    private double[][] fills;
    private final Random random = new Random();

    /**
     * Selects a random backing sample from resources/audio/backings and returns the sonification's tempo as stated in
     * the backing's filename. The selected sample and the fills associated with it are safed in the object's field vars
     * for use in getBacking().
     * @return selected tempo in bpm (quarter-note)
     * @throws AppError when there are no backing tracks matching the name schema in resources/audio/backings
     */
    public int setSamplesAndGetTempo() throws AppError {
        //TODO: I think this method of getting file names doesn't work in a JAR. Needs to be changed for release
        File directory = new File("./src/main/resources/audio/backings");
        List<String> backings = List.of(Objects.requireNonNull(directory.list()));

        backings = backings.stream().filter((string) -> string.contains("groove") || string.contains("fill")).toList();
        if (backings.isEmpty()) {
            throw new AppError("Fehler bei der Backing-Track Auswahl: Kein Backing-Track gefunden");
        }

        String randomSample = backings.get( random.nextInt(backings.size()) );
        String[] metaData = randomSample.split("_");
        // all backings have to be named like BPM_LENGTH-IN-BARS_TYPE_INDEX_NAME.wav

        // filters out all backings where NAME doesn't match chosen file
        backings = backings.stream().filter((string) -> string.contains(metaData[4])).toList();

        String grooveName = backings.stream().filter((string) -> string.contains("groove")).toList().get(0);
        groove = SampleLoader.loadBackingSample(grooveName);

        List<String> fillNames = backings.stream().filter((string) -> string.contains("fill")).toList();
        fills = new double[fillNames.size()][];
        for (int i = 0; i < fillNames.size(); i++) {
            fills[i] = SampleLoader.loadBackingSample(fillNames.get(i));
        }

        return Integer.parseInt(metaData[0]);
    }

    /**
     * Gets the previously selected backing track and loops it for the desired length,
     * alternating with a corresponding fill.
     * @param bars length of sonification in multiple of four 4/4-bars of given tempo
     * @return audio line of randomly selected backing groove and fill
     */
    public double[] getBacking( int bars ) {
        int seconds = (int) Math.ceil( (bars * 4) / (Constants.TEMPO / 60f) );
        double[] out = new double[seconds * Constants.SAMPLE_RATE * Constants.CHANNEL_NO];

        int bar = 0, offset = 0;
        while (bar < bars) {
            double[] sample = chooseSample(bar);

            System.arraycopy(sample, 0, out, offset, sample.length);

            offset += sample.length;
            bar += SAMPLE_BARS;
        }

        return out;
    }


    /**
     * All backing samples have to be of equal length SAMPLE_BARS in bars of the tempo in the filename, including the fills.
     * There can only be one groove of a given name, but it can have multiple fills associated with it.
     * They have the same name and simply increment the index in their filename.
     * @param currentBar the starting bar of the sample to be chosen
     */
    private double[] chooseSample(int currentBar) {
        if (currentBar % 8 == 4) {
            return fills[random.nextInt(fills.length)];
        } else {
            return groove;
        }
    }
}