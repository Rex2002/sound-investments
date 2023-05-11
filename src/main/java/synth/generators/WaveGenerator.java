package synth.generators;

import synth.envelopes.Envelope;

public interface WaveGenerator {
    short[] generate(double freq, int duration, int amplitude);
    short[] generate(double[] freq, int duration, int amplitude);
    short[] generate(double[] freq, int duration, int amplitude, Envelope env);
    short[] generate(double[] freq, int duration, int amplitude, double modFactor);
    short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor);
    short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor, Envelope modEnv);

}