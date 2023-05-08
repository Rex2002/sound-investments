package preDev;

// freq: 440,  493.88,  523.25,  587.33,  659.25,  698.46,  783.99,  880.00


import lombok.Data;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;
import java.util.Arrays;

public class Test {
    @Data
    private static class PhaseContainer {
        private double phase;
        private double ret;
    }
    static int SAMPLE_RATE = 44100;
    static int CHANNEL_NO = 2;
    static String waveFileName = "...path...to...res/Casio-MT-45-Beguine.wav";
    static String waveFileFunk = "...path...to...res/Yamaha-PSS-280-Funk.wav";

    public static void main(String[] args){
        System.out.println("Hello Sound");
        new Test();
    }

    public Test(){
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, CHANNEL_NO, true, true);

        try{
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            System.out.println("Audio Format: " + af);
            sdl.open(af);
            sdl.start();
            //short[] sine = createSine(440, 8, 127);
            //double[] lfo = createDoubleSine(1, 4, 1);
            ADSR adsr = new ADSR(0.2, 0.2, 0.5, 0.3);
            //boolean[] onOffFilterTest = new boolean[]{true, true, false, false, false, false, false, false};
            //onOffFilterTest = new boolean[]{true, false};
            //short[] sine = Effect.echo(Effect.onOffFilter(createSine(new double[]{440,  493.88,  523.25,  587.33, 440,  493.88,  523.25,  587.33 }, 8, 8000, adsr), onOffFilterTest), 0.5, 44100);
            //short[] sine = Effect.echo(createSine(new double[]{440,  493.88,  523.25,  587.33 }, 8, 127, adsr), 0.9, 15000);
            short[] sine1 = createSine(new double[]{440,  493.88,  523.25,  587.33 }, 4, 16383, adsr);
            short[] sine2 = createSine(new double[]{523.25,  587.33,  659.25,  698.46, }, 8, 12000, adsr);
            short[] addedSine = addArrays(sine1, sine2);

            // TODO transform every method to stereo-audio format!!!
            // Currently stereo samples can be played, but sounds a bit weird and is only half the speed
            short[] drumSample = SampleLoader.loadSample(waveFileName);
            short[] funkSample = SampleLoader.loadSample(waveFileFunk);
            short[] out1 = addArrays(addedSine, drumSample);
            short[] out2 = addArrays(out1, funkSample, drumSample.length);

            // mod freq factor of 1.5 seems to resemble a clarinet - though rather rough, could not yet figure out how to add more harmonics
            // TODO add calculation to actually play given freq when modulation and not just gcd of carrier and modulation frequency
            short[] mSine = createModulatedSine(new double[]{440,  493.88,  523.25,  587.33 }, 4, 16383, adsr, 1.5);
            short[] sq = createSquare(new double[]{440,  493.88  }, 2, 8000, adsr);
            short[] sq2 = createSquare(new double[]{523.25,  587.33}, 2, 16383, adsr);
            short[] sw = createSawtooth(new double[]{440,  493.88,  523.25,  587.33}, 4, 8191, adsr);
            //short[] combined = multiplyArrays(sine, lfo);
            EventQueue.invokeLater(() -> {
                FrequencyChart c = new FrequencyChart(Arrays.copyOfRange(sine1, 0, 44100), 1, "Sine1");
                FrequencyChart c0 = new FrequencyChart(Arrays.copyOfRange(sine2, 0, 44100), 1, "Sine2");
                FrequencyChart c1 = new FrequencyChart(Arrays.copyOfRange(addedSine, 0, 44100), 1, "Added");
                FrequencyChart c2 = new FrequencyChart(Arrays.copyOfRange(sw,0, 44100),1, "Sawtooth");
                c.setVisible(true);
                //c0.setVisible(true);
                //c1.setVisible(true);
                //c2.setVisible(true);
            });
            //play(sdl, drumSample);
            //play(sdl, addedSine);
            //play(sdl, out1);
            play(sdl, out2);

            sdl.drain();
            sdl.close();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    // creates a sine wave of given frequency, duration and max amplitude
    private short[] createSine(double[] freq, int duration, int amplitude){
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        double phase = 0;
        for(int i = 0; i < sin.length; i += 2){
            phase = this.advancePhaseSine(phase, freq[(int) (((double) i/sin.length) * freq.length)]);
            sin[i] = (short) (Math.sin( phase ) * amplitude);
            sin[i+1] = (short) (Math.sin(phase) * amplitude);
        }
        return sin;
    }

    private short[] createSine(double[] freq, int duration, int amplitude, ADSR env){
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
        double phase = 0;
        for(int i = 0; i < sin.length; i += 2){
            phase = this.advancePhaseSine(phase, freq[(int) (((double) i/sin.length) * freq.length)]);
            sin[i] = (short) (Math.sin( phase ) * amplitude * env.getAmplitudeFactor(i));
            sin[i+1] = (short) (Math.sin(phase) * amplitude * env.getAmplitudeFactor(i));
        }
        return sin;
    }

    private short[] createModulatedSine(double[] freq, int duration, int amplitude, ADSR env, double modFreqFactor){
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
        double phase = 0;
        double mPhase = 0; // modulation phase

        for(int i = 0; i < sin.length; i += 2){
            phase = this.advancePhaseSine(phase, freq[(int) (((double) i / sin.length) * freq.length)]);
            mPhase = this.advancePhaseSine(mPhase, freq[(int) (((double) i / sin.length) * freq.length)] * modFreqFactor);
            sin[i] = (short) (Math.sin(phase * Math.sin(mPhase)) * amplitude * env.getAmplitudeFactor(i));
            sin[i+1] = (short) (Math.sin(phase * Math.sin(mPhase)) * amplitude * env.getAmplitudeFactor(i));
        }
        return sin;
    }

    private short[] createSquare(double[] freq, int duration, int amplitude){
        short[] sq = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        System.out.println("freq: " + Arrays.toString(freq));
        for(int i = 0; i < sq.length; i += 2){
            this.advancePhaseSquare(phases, freq[(int) (((double) i / sq.length) * freq.length)]);
            sq[i] = (short) (phases.ret * amplitude);
            sq[i+1] = (short) (phases.ret * amplitude);
        }
        return sq;
    }

    private short[] createSquare(double[] freq, int duration, int amplitude, ADSR env){
        short[] sq = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sq.length);
        env.setNoOfTones(freq.length);
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        System.out.println("freq: " + Arrays.toString(freq));
        for(int i = 0; i < sq.length-1; i += 2){
            this.advancePhaseSquare(phases, freq[(int) (((double) i / sq.length) * freq.length)]);
            sq[i] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
            sq[i+1] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));

        }
        return sq;
    }

    private short[] createSawtooth(double[] freq, int duration, int amplitude){
        short[] sw = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for(int i = 0; i < sw.length; i += 2){
            this.advancePhaseSawtooth(phases, freq[(int) (((double) i / sw.length) * freq.length)]);
            sw[i] = (short) (phases.ret * amplitude);
            sw[i+1] = (short) (phases.ret * amplitude);
        }
        return sw;
    }

    private short[] createSawtooth(double[] freq, int duration, int amplitude, ADSR env){
        short[] sw = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sw.length);
        env.setNoOfTones(freq.length);
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for(int i = 0; i < sw.length; i += 2){
            this.advancePhaseSawtooth(phases, freq[(int) (((double) i / sw.length) * freq.length)]);

            sw[i] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
            sw[i+1] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
        }
        return sw;
    }

    // creates a sine wave in double format of given frequency, duration and max amplitude
    private double[] createDoubleSine(int freq, int duration, int amplitude){
        double[] sin = new double[duration * SAMPLE_RATE * CHANNEL_NO];
        double samplingInterval = (double) SAMPLE_RATE/freq;
        System.out.println("Frequency of signal: " + freq + " hz");
        System.out.println("Sampling interval: " + samplingInterval + " hz");
        for(int i = 0; i < sin.length; i += 2){
            double angle = ((2*Math.PI)/(samplingInterval)) * i;  // full circle: 2*Math.PI -> one step: divide by sampling interval
            //double lfo = ((2*Math.PI)/lfoSamplingInterval) * i;

            sin[i] = ((Math.sin(angle)) * (amplitude));
            sin[i+1] = ((Math.sin(angle)) * (amplitude));
        }
        return sin;
    }
    public static void play(SourceDataLine s, short[] data){
        byte[] out = new byte[data.length * 2];
        for(int p = 0; p < data.length; p++){
            out[2*p] = (byte) ((data[p] >> 8) & 0xFF);
            out[2*p+1] = (byte) (data[p] & 0xFF);
        }
        s.write(out, 0, out.length);
    }

    private void play(SourceDataLine s, byte[] data){
        s.write(data, 0, data.length);
    }

    private double advancePhaseSine(double phase, double freq){
        phase += 2 * Math.PI * freq/SAMPLE_RATE;
        while (phase > 2 * Math.PI){
            phase -= 2* Math.PI;
        }
        while (phase < 0){
            phase += 2 * Math.PI;
        }
        return phase;
    }

    private void advancePhaseSquare(PhaseContainer phases, double freq){
        phases.phase += freq/SAMPLE_RATE;

        while (phases.phase > 1.0f){
            phases.phase -= 1.0f;
        }
        while (phases.phase < 0.0f){
            phases.phase += 1.0f;
        }
        if (phases.phase >= 0.5f){
            phases.ret = 1.0f;
        }
        else{
            phases.ret = -1.0f;
        }
    }
    private void advancePhaseSawtooth(PhaseContainer phases, double freq){
        phases.phase += freq/SAMPLE_RATE;
        while(phases.phase > 1.0f)
            phases.phase -= 1.0f;

        while(phases.phase < 0.0f)
            phases.phase += 1.0f;
        phases.ret = (phases.phase * 2) - 1;
    }

    private static short[] addArrays(short[] first, short[] second) {
        return addArrays(first, second, 0);
    }

    private static short[] addArrays(short[] first, short[] second, int start) {
        System.out.println("Adding arrays of lengths " + first.length + " and " + second.length + ", starting at " + start);
        // if start is zero, it does not matter which array is longer.
        // if start is not zero, we assume that the second array is meant to be added at the given position
        if(start != 0 && first.length < second.length + start){
            throw new RuntimeException("Illegal array addition, length not matching!");
        }
        int maxLength = Math.max(first.length, second.length);
        int minLength = Math.min(first.length, second.length);
        double resizingFactor = (double) 16383 / Math.max(findMax(first), findMax(second));
        short[] result = new short[maxLength];

        for( int i = 0; i < start; i++){
            result[i] = (short) (first[i] * resizingFactor);
        }

        for (int i = start; i < minLength + start; i++) {
            short secondValue = (short) (second[i-start] * resizingFactor);
            result[i] = (short) (first[i] * resizingFactor + second[i-start] * resizingFactor);
        }
        if(maxLength == first.length){
            for(int i = minLength + start; i<maxLength; i++) {
                result[i] = (short) (first[i] * resizingFactor);
            }
        }
        else{
            System.out.println("Second array is longer");
            for(int i = minLength; i<maxLength; i++) {
                result[i] = (short) (second[i] * resizingFactor);
            }

        }
        return result;
    }

    private static short findMax(short[] array){
        short max = 0;
        for(short k : array){
            if(Math.abs(k) > max){
                max = (short) Math.abs(k);
            }
        }
        System.out.println("Found max value: " + max);
        return max;
    }

    @Deprecated
    private static short[] multiplyArrays(short[] first, double[] second) {
        int maxLength = Math.max(first.length, second.length);
        int minLength = Math.min(first.length, second.length);
        short[] result = new short[maxLength];

        for (int i = 0; i < minLength; i++) {
            result[i] = (short) (first[i] * second[i]);
        }
        if(maxLength == first.length){
            System.arraycopy(first, minLength, result, minLength, maxLength - minLength);
        }
        else{
            System.arraycopy(second, minLength, result, minLength, maxLength - minLength);
        }

        return result;
    }
}