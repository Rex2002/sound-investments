package audio.mixer;

import app.AppError;
import audio.Constants;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Backing {
    private final int SAMPLE_BARS = 4;
    private final int bars;
    private double[] groove;
    private double[][] fills;
    private final Random random = new Random();

    public Backing(int beats) {
        // int division is fine here because beats will always be multiple of 4
        this.bars = beats / 4;
    }

    public double[] getBacking() throws AppError {
        setSamplesRandomly();

        int seconds = (int) Math.ceil( (bars * 4) / (Constants.TEMPO / 60f) );
        double[] out = new double[seconds * Constants.SAMPLE_RATE * Constants.CHANNEL_NO];

        int bar = 0;
        while (bar < bars) {
            double[] sample = chooseSample(bar);

            int offset = bar * (Constants.SAMPLE_RATE * Constants.CHANNEL_NO);
            System.arraycopy(sample, 0, out, offset, sample.length);

            bar += SAMPLE_BARS;
        }

        return out;
    }

    private void setSamplesRandomly() throws AppError {
        //TODO: I think this method of getting file names doesn't work in a JAR. Needs to be changed for release
        File directory = new File("./src/main/resources/audio/backings");
        List<String> backings = List.of(Objects.requireNonNull(directory.list()));

        backings = backings.stream().filter((string) -> string.contains("groove") || string.contains("fill")).toList();
        backings = backings.stream().filter( (string) -> string.startsWith( Integer.toString(Constants.TEMPO) ) ).toList();
        if (backings.isEmpty()) {
            throw new AppError("Encountered error while selecting backing track: No valid backings of matching tempo found");
        }

        // all backings have to be named like BPM_LENGTH-IN-BARS_TYPE_INDEX_NAME.wav
        String[] metaData = backings.get( random.nextInt(backings.size()) ).split("_");

        // filters out all backings where NAME doesn't match chosen file
        backings = backings.stream().filter((string) -> string.contains(metaData[4])).toList();

        String grooveName = backings.stream().filter((string) -> string.contains("groove")).toList().get(0);
        groove = SampleLoader.loadBackingSample(grooveName);

        List<String> fillNames = backings.stream().filter((string) -> string.contains("fill")).toList();
        fills = new double[fillNames.size()][];
        for (int i = 0; i < fillNames.size(); i++) {
            fills[i] = SampleLoader.loadBackingSample(fillNames.get(i));
        }
    }


    /**
     * this currently assumes that all backing samples are of length 4, might be expanded
     */
    private double[] chooseSample(int bar) {
        if (bar % 8 == 5) {
            return fills[random.nextInt(fills.length)];
        } else {
            return groove;
        }
    }
}
