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

    public int setSamplesAndGetTempo() throws AppError {
        //TODO: I think this method of getting file names doesn't work in a JAR. Needs to be changed for release
        File directory = new File("./src/main/resources/audio/backings");
        List<String> backings = List.of(Objects.requireNonNull(directory.list()));

        backings = backings.stream().filter((string) -> string.contains("groove") || string.contains("fill")).toList();
        if (backings.isEmpty()) {
            throw new AppError("Encountered error while selecting backing track: No valid backings found");
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
     * this currently assumes that all backing samples are of length 4
     */
    private double[] chooseSample(int bar) {
        if (bar % 8 == 4) {
            return fills[random.nextInt(fills.length)];
        } else {
            return groove;
        }
    }
}
