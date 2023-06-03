package audio.mixer;

public class Mixer {
    /**
     * Mixes the given audio streams together.
     *
     * @param audioStreams array of double-arrays to be mixed
     * @return mixed audio stream as double-array
     */
    public static double[] mixAudioStreams(double[][] audioStreams) {
        int maxLength = 0;
        for (double[] stream : audioStreams) {
            maxLength = Math.max(maxLength, stream.length);
        }

        double[] out = new double[maxLength];

        for (double[] audioStream : audioStreams) {
            for (int j = 0; j < audioStream.length; j++) {
                out[j] += audioStream[j];
            }
        }
        return out;
    }
}