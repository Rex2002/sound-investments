package audio.mixer;

//import com.groupdocs.metadata.Metadata;
//import com.groupdocs.metadata.core.WavRootPackage;
//import com.groupdocs.metadata.internal.c.a.s.internal.nb.Au;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

public class SampleLoader {
    /**
     * loads specified audio sample from resources/audio/impacts directory
     */
    public static double[] loadEventSample(String filename) {
        return loadSample("/audio/impacts/" + filename);
    }

    /**
     * loads specified audio sample from resources/audio/backings directory
     */
    public static double[] loadBackingSample(String filename) {
        return loadSample("/audio/backings/" + filename);
    }

    private static double[] loadSample(String filename) {

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(SampleLoader.class.getResourceAsStream(filename))));
            AudioFormat af = audioStream.getFormat();
            if(af.getFrameSize() != 4 || af.getSampleRate() != 44100 || af.isBigEndian()) {
                throw new RuntimeException("Illegal audio format in sample" + filename);
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
            throw new RuntimeException("error while reading sample file: " + e.getMessage());
        }

    }

// INFO: only needed for debugging, only works when adding groupdocs to mvn and after uncommenting respective imports
//    public static void printAudioFormat(String filename){
//        try (Metadata metadata = new Metadata(filename))
//        {
//            WavRootPackage root = metadata.getRootPackageGeneric();
//            System.out.println(root.getWavPackage().getBitsPerSample()); // Bits per Sample
//            System.out.println(root.getWavPackage().getBlockAlign()); // Block Align
//            System.out.println(root.getWavPackage().getByteRate()); // Byte Rate
//            System.out.println(root.getWavPackage().getNumberOfChannels()); // No. of Channels
//            System.out.println(root.getWavPackage().getAudioFormat()); // Audio Format
//            System.out.println(root.getWavPackage().getSampleRate()); // Sample Rate
//        }
//    }
}