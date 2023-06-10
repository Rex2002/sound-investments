package audio.mixer;

public class Mixer {
    /**
     * Mixes the given audio streams together. <br/>
     * The numberEvLines is used to apply the EVENT_LINES_MIX_FACTOR to each EventInstrument, <br/>
     * while the indexBacking is used to apply the BACKING_MIX_FACTOR to the backing-track
     * @param audioStreams array of double-arrays to be mixed
     * @param numberEvLines describes number of event lines
     * @param indexBacking index of the backing track
     * @return mixed audio stream as double-array
     */
    public static double[] mixAudioStreams(double[][] audioStreams, int numberEvLines, int indexBacking) {
        double EVENT_LINES_MIX_FACTOR = 0.65;
        double BACKING_MIX_FACTOR = 0.95;
        int maxLength = 0;
        for (double[] stream : audioStreams) {
            maxLength = Math.max(maxLength, stream.length);
        }

        double[] out = new double[maxLength];

        for (int i = 0; i < audioStreams.length; i++) {
            double[] audioStream = audioStreams[i];

            double mixFactor = 1;

            //reduce volume of event instruments
            if (i < numberEvLines) {
                mixFactor = EVENT_LINES_MIX_FACTOR;
            }
            // reduce volume of backing
            if (i == indexBacking) {
                mixFactor = BACKING_MIX_FACTOR;
            }

            for (int j = 0; j < audioStream.length; j++) {
                out[j] += mixFactor * audioStream[j];
            }
        }
        return out;
    }
}