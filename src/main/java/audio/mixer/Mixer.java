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
        int minStartPosition = maxLength;
        for (int i = 1; i < startPositions.length; i++) {
            minStartPosition = Math.min(minStartPosition, startPositions[i]);
        }

        // check if the start positions are valid
        if (minStartPosition < 0) {
            throw new MixerException("Illegal start position, must be >= 0!");
        } else if (minStartPosition > maxLength) {
            throw new MixerException("Illegal start position, must be <= maxLength!");
        }

        // because we want to add all arrays on top of the first one, we need to add the values of the first array
        // to the result array up to the first startPosition (minStartPosition)
        System.arraycopy(audioStreams.get(0), 0, result, 0, minStartPosition);

        for (int i = 0; i < audioStreams.size(); i++) {
            double[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if (startPosition != 0 && audioStream.length + startPosition > result.length) {
                throw new MixerException("Illegal array addition, length not matching! (Array " + i + " too long)");
            }

            // add the values of the array to the result array starting at the given startPosition
            for (int j = startPosition; j < audioStream.length + startPosition; j++) {
                result[j] += audioStream[j];
            }
        }

        /*// add the remaining values of the longest array
        for (int i = 0; i < audioStreams.size(); i++) {
            double[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if (maxLength == audioStream.length) {
                //check if there are multiple arrays with the maxLength
                if (maxLengthCount > 1 && i != 0) {
                    for (int j = minLength + startPosition; j < maxLength; j++) {
                        result[j] += audioStream[j];
                    }
                } else {
                    if (maxLength - (minLength + startPosition) >= 0)
                        System.arraycopy(audioStream, minLength + startPosition, result, minLength + startPosition, maxLength - (minLength + startPosition));
                }
            } else {
                for (int j = minLength; j < audioStream.length; j++) {
                    result[j] = result[j] + audioStream[j];
                }
            }
        }*/

        return result;
    }
}
