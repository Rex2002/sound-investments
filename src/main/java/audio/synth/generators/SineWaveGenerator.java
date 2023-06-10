package audio.synth.generators;

 import audio.Util;
import audio.synth.envelopes.Envelope;
import audio.synth.envelopes.OneEnvelope;
import audio.synth.envelopes.ZeroEnvelope;

public class SineWaveGenerator {

    public double[] generate(double freq, int duration, short amplitude){
        return generate(freq, duration, new double[]{amplitude});
    }
    public double[] generate(double freq, int duration, double[] amplitude) {
        return generate(new double[]{freq}, duration, amplitude);
    }
    public double[] generate(double[] freq, int duration, double[] amplitude){
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, oneEnvelope);
    }
    public double[] generate(double[] freq, int duration, double[] amplitude, Envelope env) {
        Envelope zeroEnvelope = new ZeroEnvelope();
        return generate(freq, duration, amplitude, env, 1, zeroEnvelope);
    }
    public double[] generate(double[] freq, int duration, double[] amplitude, double modFactor) {
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, oneEnvelope, modFactor);
    }
    public double[] generate(double[] freq, int duration, double[] amplitude, Envelope env, double modFactor) {
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, env, modFactor, oneEnvelope);
    }

    /**
     * generates a sound based on the provided inputs <br/>
     * @param freq array of frequencies in hz
     * @param sampleNumber total number of samples to be created
     * @param amplitude provides the amplitudes, could theoretically be any length less than sampleNumber (is relatively stretched).
     * @param env the envelope for the amplitude of the output
     * @param modFactor the modulation factor that is used to calculate the modulation frequency
     * @param modEnv the envelope that determines the impact/strenght of the modulation
     * @return an array that (if played with the correct audioFormat/audioLine) plays the notes that are given with the frequency-array distributed evenly across the whole
     *          length (repeated note values are tied)
     */
    public double[] generate(double[] freq, int sampleNumber, double[] amplitude, Envelope env, double modFactor, Envelope modEnv) {
        double[] sin = new double[sampleNumber];
        env.setSectionLen(sin.length/freq.length);
        modEnv.setSectionLen(sin.length/freq.length);
        double phase = 0;
        double mPhase = 0; // modulation phase
        double sin1, ampFactor, modAmpFactor;
        int freqIdx = -1, ampIdx;
        int sectionLen;
        int sectionOffset = 0;
        // quick explanation:
        //  freqIdx: index of the current frequency
        //  ampIdx: same as freqIdx, just for amplitude
        //  sectionLen: how long is the current frequency played (i.e. does it occur once in a row, twice, etc. in the freq. array)
        //  sectionOffset: where does the section start in samples. This is needed to always restart the enveloping-counter at zero when a frequency change happens.
        for(int i = 0; i < sin.length - 1; i += 2){
            if(freqIdx == -1 || (freq[freqIdx] != freq[Util.getRelPosition(i, sin.length, freq.length)])){
                freqIdx = Util.getRelPosition(i, sin.length, freq.length);
                sectionOffset = i;
                sectionLen = 0;
                while(freqIdx + sectionLen < freq.length && freq[freqIdx] == freq[freqIdx + sectionLen]){
                    sectionLen++;
                }
                env.setSectionLen(sin.length / freq.length * sectionLen);
                modEnv.setSectionLen(sin.length / freq.length * sectionLen);
            }

            ampIdx = Util.getRelPosition(i, sin.length, amplitude.length);

            ampFactor = env.getAmplitudeFactor(i - sectionOffset);
            modAmpFactor = modEnv.getAmplitudeFactor(i - sectionOffset);

            phase = PhaseAdvancers.advancePhaseSine(phase, freq[freqIdx]);
            mPhase = PhaseAdvancers.advancePhaseSine(mPhase, freq[freqIdx] * modFactor);

            sin1 = Math.sin(phase + Math.sin(mPhase) * modAmpFactor);
            sin[i] = sin1 * amplitude[ampIdx] * ampFactor;
            sin[i+1] = sin1 * amplitude[ampIdx] * ampFactor;
        }
        return sin;
    }
}