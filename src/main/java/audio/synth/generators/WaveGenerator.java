package audio.synth.generators;

import audio.synth.envelopes.Envelope;

public interface WaveGenerator {
    short[] generate(double freq, int duration, short amplitude);
    short[] generate(double freq, int duration, short[] amplitude);
    short[] generate(double[] freq, int duration, short[] amplitude);
    short[] generate(double[] freq, int duration, short[] amplitude, Envelope env);
    short[] generate(double[] freq, int duration, short[] amplitude, double modFactor);
    short[] generate(double[] freq, int duration, short[] amplitude, Envelope env, double modFactor);
    short[] generate(double[] freq, int duration, short[] amplitude, Envelope env, double modFactor, Envelope modEnv);

}