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
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
        modEnv.setTotalLength(sin.length);
        modEnv.setNoOfTones(freq.length);
        double phase = 0;
        double mPhase = 0; // modulation phase
        double sin1;
        double ampFactor;
        double modAmpFactor;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq[(int) (((double) i / sin.length) * freq.length)]);
            mPhase = PhaseAdvancers.advancePhaseSine(mPhase, freq[(int) (((double) i / sin.length) * freq.length)] * modFactor);
            ampFactor = env.getAmplitudeFactor(i);
            modAmpFactor = modEnv.getAmplitudeFactor(i);
            sin1 = Math.sin(phase + Math.sin(mPhase) * modAmpFactor);
            sin[i] = (short) (sin1 * amplitude[(int) (((double) i / sin.length) * amplitude.length)] * ampFactor);
            sin[i+1] = (short) (sin1 * amplitude[(int) (((double) i / sin.length) * amplitude.length)] * ampFactor);
        }
        return sin;
    }
}