package audio.synth;

// freq: 440,  493.88,  523.25,  587.33,  659.25,  698.46,  783.99,  880.00

import audio.Constants;
import audio.Util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;

import static audio.Constants.SAMPLE_RATE;


public class Test {
    public static void main(String[] args) {
        System.out.println("Hello Sound");
        new Test();
    }

    public Test() {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, Constants.CHANNEL_NO, true, true);

        try {
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            InstrumentData instrData = TestEchoValues.getTestData();
            double[] synthLine = new SynthLine(instrData, 40).synthesize();


            //double[] mSine = generator.generate(new double[] {110.0, 130.8127826502993, 110.0, 97.99885899543733, 103.82617439498628, 97.99885899543733, 123.47082531403103, 116.54094037952248, 123.47082531403103, 146.8323839587038, 146.8323839587038, 138.59131548843604, 116.54094037952248, 92.49860567790861, 87.30705785825097, 92.49860567790861, 97.99885899543733, 82.40688922821748, 73.41619197935188, 77.78174593052022, 97.99885899543733, 97.99885899543733, 97.99885899543733, 123.47082531403103, 164.81377845643496, 195.99771799087463, 184.99721135581723, 246.94165062806206, 329.6275569128699, 329.6275569128699, 329.6275569128699, 329.6275569128699, 277.1826309768721, 261.6255653005986, 220.0, 195.99771799087463, 184.99721135581723, 220.0, 293.6647679174076, 369.99442271163446, 369.99442271163446, 311.12698372208087, 369.99442271163446, 466.1637615180899, 440.0, 440.0, 391.99543598174927, 391.99543598174927, 391.99543598174927, 440.0, 391.99543598174927, 523.2511306011972, 554.3652619537442, 587.3295358348151, 659.2551138257398, 622.2539674441618, 622.2539674441618, 830.6093951598903, 987.7666025122483, 932.3275230361799}, 60, new double[] { 15000 }, 2 / 3f);
            EventQueue.invokeLater(() -> {
                        //FrequencyChart c = new FrequencyChart(Arrays.copyOfRange(synthLine, 0, synthLine.length), 100,"Unfiltered");
             //c.setVisible(true);

            });
            //playWithControls(sdl, mSine);
            play(sdl, Util.scaleToShort(synthLine));
            System.out.println("finished playing");
            sdl.drain();
            sdl.close();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static void play(SourceDataLine s, short[] data) {
        byte[] out = new byte[data.length * 2];
        for (int p = 0; p < data.length; p++) {
            out[2 * p] = (byte) ((data[p] >> 8) & 0xFF);
            out[2 * p + 1] = (byte) (data[p] & 0xFF);
        }
        s.write(out, 0, out.length);
    }

    private void play(SourceDataLine s, byte[] data) {
        s.write(data, 0, data.length);
    }
}