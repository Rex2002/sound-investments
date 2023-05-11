package synth.generators;

import synth.envelopes.Envelope;

import static synth.Test.CHANNEL_NO;
import static synth.Test.SAMPLE_RATE;


//TODO or discuss: amplitude generally as short
public class SineWaveGenerator implements WaveGenerator{
    @Override
    public short[] generate(double freq, int duration, int amplitude) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        double phase = 0;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq);
            sin[i] = (short) (Math.sin( phase ) * amplitude);
            sin[i+1] = (short) (Math.sin(phase) * amplitude);
        }
        return sin;
    }

    @Override
    public short[] generate(double[] freq, int duration, int amplitude){
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        double phase = 0;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq[(int) (((double) i/sin.length) * freq.length)]);
            sin[i] = (short) (Math.sin( phase ) * amplitude);
            sin[i+1] = (short) (Math.sin(phase) * amplitude);
        }
        return sin;
    }
    @Override
    public short[] generate(double[] freq, int duration, int amplitude, Envelope env) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
        double phase = 0;
        double ampFactor;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq[(int) (((double) i/sin.length) * freq.length)]);
            ampFactor = env.getAmplitudeFactor(i);
            sin[i] = (short) (Math.sin( phase ) * amplitude * ampFactor);
            sin[i+1] = (short) (Math.sin(phase) * amplitude * ampFactor);
        }
        return sin;
    }

    @Override
    public short[] generate(double[] freq, int duration, int amplitude, double modFactor) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        double phase = 0;
        double mPhase = 0; // modulation phase
        double sin1;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq[(int) (((double) i / sin.length) * freq.length)]);
            mPhase = PhaseAdvancers.advancePhaseSine(mPhase, freq[(int) (((double) i / sin.length) * freq.length)] * modFactor);
            sin1 = Math.sin(phase + Math.sin(mPhase));

            sin[i] = (short) (sin1 * amplitude);
            sin[i+1] = (short) (sin1 * amplitude);
        }
        return sin;
    }

    @Override
    public short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
        double phase = 0;
        double mPhase = 0; // modulation phase
        double sin1;
        double ampFactor;
        for(int i = 0; i < sin.length; i += 2){
            phase = PhaseAdvancers.advancePhaseSine(phase, freq[(int) (((double) i / sin.length) * freq.length)]);
            mPhase = PhaseAdvancers.advancePhaseSine(mPhase, freq[(int) (((double) i / sin.length) * freq.length)] * modFactor);
            sin1 = Math.sin(phase + Math.sin(mPhase));
            ampFactor = env.getAmplitudeFactor(i);
            sin[i] = (short) (sin1 * amplitude * ampFactor);
            sin[i+1] = (short) (sin1 * amplitude * ampFactor);
        }
        return sin;
    }

    @Override
    public short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor, Envelope modEnv) {
        short[] sin = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setTotalLength(sin.length);
        env.setNoOfTones(freq.length);
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
            sin[i] = (short) (sin1 * amplitude * ampFactor);
            sin[i+1] = (short) (sin1 * amplitude * ampFactor);
        }
        return sin;
    }
}