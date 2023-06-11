package dhbw.si.audio.mixer;

import dhbw.si.app.AppError;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

public class SampleLoader {
    /**
     * loads specified audio sample from resources/audio/impacts directory
     */
    public static double[] loadEventSample(String filename) throws AppError {
        return loadSample("/audio/impacts/" + filename);
    }

    /**
     * loads specified audio sample from resources/audio/backings directory
     */
    public static double[] loadBackingSample(String filename) throws AppError {
        return loadSample("/audio/backings/" + filename);
    }

    /**
     * loads a sample from a file and converts to the correct audioformat
     * @param filename of the sample that is to be loaded
     * @return the data array representing the sample in the desired format
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static double[] loadSample(String filename) throws AppError {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(SampleLoader.class.getResourceAsStream(filename))));
            AudioFormat af = audioStream.getFormat();
            if(af.getFrameSize() != 4 || af.getSampleRate() != 44100 || af.isBigEndian()) {
                throw new AppError("Ungültiges Audioformat in Sample " + filename);
            }
            double[] out = new double[(int) (audioStream.getFrameLength() * 2)];
            byte[] frame = new byte[4];
            for(int pos = 0; pos < audioStream.getFrameLength(); pos ++){
                audioStream.read(frame);
                out[2 * pos] = frame[1]  << 8 | frame[0] & 0xFF;
                out[2 * pos + 1] = frame[3] << 8 | frame[2] & 0xFF;
            }

            return out;
        } catch (NullPointerException | IOException | UnsupportedAudioFileException e){
            e.printStackTrace();
            throw new AppError("Error beim Auslesen des Samples " + filename);
        }

    }
}