package audio.mixer;

import java.util.ArrayList;

public class Mixer {
    /**
     * Mixes the given audio streams together.
     *
     * @param audioStreams   list of double-arrays
     * @param startPositions list of start positions for each audio stream (int[0] should always be 0)
     * @return mixed audio stream as double-array
     * @throws MixerException if the length of the audio streams does not match
     */
    public static double[] mixAudioStreams(ArrayList<double[]> audioStreams, int[] startPositions) throws MixerException {

        // find the longest array
        int maxLength = 0;
        for (double[] audioStream : audioStreams) {
            maxLength = Math.max(maxLength, audioStream.length);
        }
        // find the shortest array
        int minLength = maxLength;
        for (double[] audioStream : audioStreams) {
            minLength = Math.min(minLength, audioStream.length);
        }
        double[] result = new double[maxLength];


        // find the minimum start position (except of startPositions[0] which should always be 0)
        // check if the start positions are valid
        int minStartPosition = maxLength;
        for (int i = 1; i < startPositions.length; i++) {
            minStartPosition = Math.min(minStartPosition, startPositions[i]);
            if (startPositions[i] > maxLength) {
                throw new MixerException("Illegal start position ("+i+"), must be <= maxLength!");
            }
        }

        // check if the start positions are valid
        if (minStartPosition < 0) {
            throw new MixerException("Illegal start position, must be >= 0!");
        }


        for (int i = 0; i < audioStreams.size(); i++) {
            double[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if (audioStream.length + startPosition > result.length) {
                throw new MixerException("Illegal array addition, length not matching! (Array " + i + " too long)");
            }

            // add the values of the array to the result array starting at the given startPosition
            for (int j = startPosition; j < audioStream.length + startPosition; j++) {
                result[j] += audioStream[j];
            }
        }

        return result;
    }
}
