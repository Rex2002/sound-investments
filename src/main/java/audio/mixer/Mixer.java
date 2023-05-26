package audio.mixer;

import java.util.LinkedList;

import static audio.mixer.Util.findMax;

public class Mixer {


    public static short[] mixAudioStreams(LinkedList<short[]> audioStreams, int[] startPositions) throws MixerException{
        // if start is zero, it does not matter which array is longer.
        // if start is not zero, we assume that every array is meant to be added at the given position
        int maxLength = 0;
        for(short[] audioStream : audioStreams){
            maxLength = Math.max(maxLength, audioStream.length);
        }
        int minLength = maxLength;
        for(short[] audioStream : audioStreams){
            minLength = Math.min(minLength, audioStream.length);
        }
        System.out.println("Max length: " + maxLength);
        short[] result = new short[maxLength];
        double resizingFactor = (double) 16383 / findMax(audioStreams);

        for(int i = 0; i < audioStreams.size(); i++){
            short[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if(startPosition != 0 && audioStream.length < maxLength + startPosition){
                throw new MixerException("Illegal array addition, length not matching!");
            }
            for( int j = 0; j < startPosition; j++){
                result[j] = (short) (audioStream[j] * resizingFactor);
            }
            for (int j = startPosition; j < audioStream.length + startPosition; j++) {
                result[j] = (short) ((result[j] + audioStream[j]) * resizingFactor);
            }
        }
        // add the remaining values of the longest array
        for(int i = 0; i < audioStreams.size(); i++){
            short[] audioStream = audioStreams.get(i);
            int startPosition = startPositions[i];
            if(maxLength == audioStream.length){
                for(int j = minLength + startPosition; j<maxLength; j++) {
                    result[j] = (short) (audioStream[j] * resizingFactor);
                }
            }
            else{
                for(int j = minLength; j<audioStream.length; j++) {
                    result[j] = (short) (result[j] + audioStream[j] * resizingFactor);
                }
            }
        }

        return result;
    }
}
