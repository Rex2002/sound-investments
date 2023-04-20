package preDev;



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

    public static void main(String[] args){
        System.out.println("Hello Sound");
        new Test();
    }

    public Test(){
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        try{
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            //byte[] sine = createSine(440, 8, 127);
            //double[] lfo = createDoubleSine(1, 4, 1);
            byte[] sine = createSine(new double[]{440,  493.88,  523.25,  587.33,  659.25,  698.46,  783.99,  880.00}, 4, 127);
            byte[] sq = createSquare(new double[]{440,  493.88,  523.25,  587.33,  659.25,  698.46,  783.99,  880.00}, 4, 127);
            byte[] sw = createSawtooth(new double[]{440,  493.88,  523.25,  587.33,  659.25,  698.46,  783.99,  880.00}, 4, 127);
            //byte[] combined = multiplyArrays(sine, lfo);
            EventQueue.invokeLater(() -> {
                FrequencyChart c = new FrequencyChart(Arrays.copyOfRange(sine, 0, 2000), 1, "Raw Sine");
                FrequencyChart c1 = new FrequencyChart(Arrays.copyOfRange(sq, 0, 2000), 1, "Square");
                FrequencyChart c2 = new FrequencyChart(Arrays.copyOfRange(sw,0, 2000),1, "Sawtooth");
                c.setVisible(true);
                c1.setVisible(true);
                c2.setVisible(true);
            });
            //play(sdl, cleanSine);
            play(sdl, sine);
            play(sdl, sq);
            play(sdl, sw);
            //play(sdl, sine);

            sdl.drain();
            sdl.close();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    // creates a sine wave of given frequency, duration and max amplitude
    private byte[] createSine(int freq, int duration, int amplitude){
        byte[] sin = new byte[duration * SAMPLE_RATE];
        double samplingInterval = (double) SAMPLE_RATE/freq ;
        //double lfoSamplingInterval = (double) SAMPLE_RATE/2;
        System.out.println("Frequency of signal: " + freq + " hz");
        System.out.println("Sampling interval: " + samplingInterval + " hz");
        for(int i = 0; i < sin.length; i++){
            samplingInterval = Math.sin(2* Math.PI * i*0.000005);
            double angle = ((2*Math.PI * i)/(samplingInterval));  // full circle: 2*Math.PI -> one step: divide by sampling interval
            //double lfo = ((2*Math.PI)/lfoSamplingInterval) * i;

            sin[i] = (byte) ((Math.sin(angle)) * (amplitude) );
        }
        return sin;
    }

    private byte[] createSine(double[] freq, int duration, int amplitude){
        byte[] sin = new byte[duration * SAMPLE_RATE];
        double phase = 0;
        for(int i = 0; i < sin.length; i++){
            phase = this.advancePhaseSine(phase, freq[(int) (((double) i/sin.length) * freq.length)]);
            sin[i] = (byte) (Math.sin( phase ) * amplitude);
        }
        return sin;
    }

    private byte[] createSquare(double[] freq, int duration, int amplitude){
        byte[] sq = new byte[duration * SAMPLE_RATE];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        System.out.println("freq: " + Arrays.toString(freq));
        for(int i = 0; i < sq.length; i++){
            phases = this.advancePhaseSquare(phases, freq[(int) (((double) i/sq.length) * freq.length)]);
            sq[i] = (byte) (phases.ret * amplitude);
        }
        return sq;
    }

    private byte[] createSawtooth(double[] freq, int duration, int amplitude){
        byte[] sw = new byte[duration * SAMPLE_RATE];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for(int i = 0; i < sw.length; i++){
            System.out.println("forwarded freq: " + freq[(int) ((double) i/sw.length) * freq.length]);
            phases = this.advancePhaseSawtooth(phases, freq[(int) (((double) i/sw.length) * freq.length)]);

            sw[i] = (byte) (phases.ret * amplitude);
        }
        return sw;
    }

    // creates a sine wave in double format of given frequency, duration and max amplitude
    private double[] createDoubleSine(int freq, int duration, int amplitude){
        double[] sin = new double[duration * SAMPLE_RATE];
        double samplingInterval = (double) SAMPLE_RATE/freq;
        double lfoSamplingInterval = (double) SAMPLE_RATE/2;
        System.out.println("Frequency of signal: " + freq + " hz");
        System.out.println("Sampling interval: " + samplingInterval + " hz");
        for(int i = 0; i < sin.length; i++){
            double angle = ((2*Math.PI)/(samplingInterval)) * i;  // full circle: 2*Math.PI -> one step: divide by sampling interval
            //double lfo = ((2*Math.PI)/lfoSamplingInterval) * i;

            sin[i] = ((Math.sin(angle)) * (amplitude));
        }
        return sin;
    }
    private void play(SourceDataLine s, byte[] data){
        s.write(data, 0, data.length);
    }

    @Deprecated
    private static byte[] addArrays(byte[] first, byte[] second) {
        int maxLength = Math.max(first.length, second.length);
        int minLength = Math.min(first.length, second.length);
        byte[] result = new byte[maxLength];

        for (int i = 0; i < minLength; i++) {
            result[i] = (byte) (first[i] + second[i]);
        }
        if(maxLength == first.length){
            System.arraycopy(first, minLength, result, minLength, maxLength - minLength);
        }
        else{
            System.arraycopy(second, minLength, result, minLength, maxLength - minLength);
        }

        return result;
    }

    @Deprecated
    private static byte[] multiplyArrays(byte[] first, double[] second) {
        int maxLength = Math.max(first.length, second.length);
        int minLength = Math.min(first.length, second.length);
        byte[] result = new byte[maxLength];

        for (int i = 0; i < minLength; i++) {
            result[i] = (byte) (first[i] * second[i]);
        }
        if(maxLength == first.length){
            System.arraycopy(first, minLength, result, minLength, maxLength - minLength);
        }
        else{
            System.arraycopy(second, minLength, result, minLength, maxLength - minLength);
        }

        return result;
    }

    @Deprecated
    private static byte[] multiplyArrays(byte[] first, byte[] second) {
        int maxLength = Math.max(first.length, second.length);
        int minLength = Math.min(first.length, second.length);
        byte[] result = new byte[maxLength];

        for (int i = 0; i < minLength; i++) {
            result[i] = (byte) (first[i] * second[i]);
        }
        if(maxLength == first.length){
            System.arraycopy(first, minLength, result, minLength, maxLength - minLength);
        }
        else{
            System.arraycopy(second, minLength, result, minLength, maxLength - minLength);
        }

        return result;
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
    private PhaseContainer advancePhaseSquare(PhaseContainer phases, double freq){
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
        return phases;
    }

    private PhaseContainer advancePhaseSawtooth(PhaseContainer phases, double freq){
        phases.phase += freq/SAMPLE_RATE;
        while(phases.phase > 1.0f)
            phases.phase -= 1.0f;

        while(phases.phase < 0.0f)
            phases.phase += 1.0f;
        phases.ret = (phases.phase * 2) - 1;
        return phases;
    }
}
