package audio.mixer;

import java.util.LinkedList;

public class Mixer {

    /**
     * Finds the maximum value of all arrays in the given list.
     *
     * @param arrayList list of short-arrays
     * @return maximum value as short
     */
    private static short findMax(LinkedList<short[]> arrayList) {
        short max = 0;
        for (short[] array : arrayList) {
            for (short k : array) {
                if (Math.abs(k) > max) {
                    max = (short) Math.abs(k);
                }
            }
        }
        //System.out.println("Found max value: " + max);
        return max;
    }



    /**
     * Mixes the given audio streams together.
     *
     * @param audioStreams   list of short-arrays
     * @param startPositions list of start positions for each audio stream (int[0] should always be 0)
     * @return mixed audio stream as short-array
     * @throws MixerException if the length of the audio streams does not match
     */
    public static short[] mixAudioStreams(LinkedList<short[]> audioStreams, int[] startPositions) throws MixerException {

        // find the longest array
        int maxLength = 0;
        for (short[] audioStream : audioStreams) {
            maxLength = Math.max(maxLength, audioStream.length);
        }
        // find the shortest array
        int minLength = maxLength;
        for (short[] audioStream : audioStreams) {
            minLength = Math.min(minLength, audioStream.length);
        }
        short[] result = new short[maxLength];

        // To prevent Short overflow (Speakers will not be able to play the sound) we need to resize the values
        // Short has a range of -32768 to 32767 and by dividing the short.MaxValue/2 by the maximum value of the
        // audio streams we get the factor by which we need to multiply the values to fit into the range.
        //TODO add list length to the calculation
        double resizingFactor = (double) 16383 / findMax(audioStreams);


        // find the minimum start position (except of startPositions[0] which should always be 0)
        int minStartPosition = maxLength;
        for (int i = 1; i < startPositions.length; i++) {
            minStartPosition = Math.min(minStartPosition, startPositions[i]);
        }

        // check if the start positions are valid
        if (minStartPosition < 0) {
            throw new MixerException("Illegal start position, must be >= 0!");
        } else if (minStartPosition > maxLength) {
            throw new MixerException("Illegal start position, must be <= minLength!");
        }

        // because we want to add all arrays on top of the first one, we need to add the values of the first array
        // to the result array up to the first startPosition (minStartPosition)
        for (int j = 0; j < minStartPosition; j++) {
            result[j] = (short) (audioStreams.get(0)[j] * resizingFactor);
        }

        for (int i = 0; i < audioStreams.size(); i++) {
            short[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            // TODO check if length of array + startPosition is bigger than length of result array
            if (startPosition != 0 && audioStream.length + startPosition > result.length) {
                throw new MixerException("Illegal array addition, length not matching! (Array " + i + " too long)");
            }

            // add the values of the array to the result array starting at the given startPosition
            for (int j = startPosition; j < audioStream.length + startPosition; j++) {
                result[j] = (short) ((result[j] + audioStream[j]) * resizingFactor);
            }
        }

        int maxLengthCount = 0;
        //check if there are multiple arrays with the maxLength
        for (short[] audioStream : audioStreams) {
            if (maxLength == audioStream.length) {
                maxLengthCount++;
            }
        }

        // add the remaining values of the longest array
        for (int i = 0; i < audioStreams.size(); i++) {
            short[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if (maxLength == audioStream.length) {
                //check if there are multiple arrays with the maxLength
                if (maxLengthCount > 1 && i != 0) {
                    for (int j = minLength + startPosition; j < maxLength; j++) {
                        result[j] = (short) ((result[j] + audioStream[j]) * resizingFactor);
                    }
                } else {
                    for (int j = minLength + startPosition; j < maxLength; j++) {
                        result[j] = (short) (audioStream[j] * resizingFactor);
                    }
                }
            } else {
                for (int j = minLength; j < audioStream.length; j++) {
                    result[j] = (short) (result[j] + audioStream[j] * resizingFactor);
                }
            }
        }

        return result;
    }
}
