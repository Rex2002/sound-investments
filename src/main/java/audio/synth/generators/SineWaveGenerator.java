package audio.synth.generators;

import audio.synth.envelopes.Envelope;
import audio.synth.envelopes.OneEnvelope;
import audio.synth.envelopes.ZeroEnvelope;

import static audio.Constants.CHANNEL_NO;
import static audio.Constants.SAMPLE_RATE;


//TODO or discuss: amplitude generally as short
//TODO refactor to reduce code duplication
public class SineWaveGenerator implements WaveGenerator{

    public short[] generate(double freq, int duration, short amplitude){
        return generate(freq, duration, new short[]{amplitude});
    }
    @Override
    public short[] generate(double freq, int duration, short[] amplitude) {
        return generate(new double[]{freq}, duration, amplitude);
    }

    @Override
    public short[] generate(double[] freq, int duration, short[] amplitude){
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, oneEnvelope);
    }
    @Override
    public short[] generate(double[] freq, int duration, short[] amplitude, Envelope env) {
        Envelope zeroEnvelope = new ZeroEnvelope();
        return generate(freq, duration, amplitude, env, 1, zeroEnvelope);
    }

    @Override
    public short[] generate(double[] freq, int duration, short[] amplitude, double modFactor) {
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, oneEnvelope, modFactor);
    }

    @Override
    public short[] generate(double[] freq, int duration, short[] amplitude, Envelope env, double modFactor) {
        Envelope oneEnvelope = new OneEnvelope();
        return generate(freq, duration, amplitude, env, modFactor, oneEnvelope);
    }

    @Override
    public short[] generate(double[] freq, int duration, short[] amplitude, Envelope env, double modFactor, Envelope modEnv) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setSectionLen(sin.length/freq.length);
        modEnv.setSectionLen(sin.length/freq.length);
        double phase = 0;
        double mPhase = 0; // modulation phase
        double sin1, ampFactor, modAmpFactor;
        int freqIdx = -1, ampIdx, sectionLen, sectionOffset = 0;
        // quick explanation:
        //  freqIdx: index of the current frequency
        //  ampIdx: same as freqIdx, just for amplitude
        //  sectionLen: how long is the current frequency played (i.e. does it occur once in a row, twice, etc. in the freq. array)
        //  sectionOffset: where does the section start in samples. This is needed to always restart the enveloping-counter at zero when a frequency change happens.
        for(int i = 0; i < sin.length; i += 2){
            if(freqIdx == -1 || (freq[freqIdx] != freq[(int) (((double) i / sin.length) * freq.length)])){
                freqIdx = (int) (((double) i / sin.length) * freq.length);
                sectionOffset = i;
                sectionLen = 0;
                while(freqIdx + sectionLen < freq.length && freq[freqIdx] == freq[freqIdx + sectionLen]){
                    sectionLen++;
                }
                env.setSectionLen(sin.length / freq.length * sectionLen);
            }

            ampIdx = (int) (((double) i / sin.length) * amplitude.length);

            ampFactor = env.getAmplitudeFactor(i - sectionOffset);
            modAmpFactor = modEnv.getAmplitudeFactor(i - sectionOffset);

            phase = PhaseAdvancers.advancePhaseSine(phase, freq[freqIdx]);
            mPhase = PhaseAdvancers.advancePhaseSine(mPhase, freq[freqIdx] * modFactor);

            sin1 = Math.sin(phase + Math.sin(mPhase) * modAmpFactor);
            sin[i] = (short) (sin1 * amplitude[ampIdx] * ampFactor);
            sin[i+1] = (short) (sin1 * amplitude[ampIdx] * ampFactor);
        }
        return sin;
    }
}