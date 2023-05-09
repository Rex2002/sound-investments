package preDev.generators;

import preDev.ADSR;

public interface WaveGenerator {
    short[] generate(double freq, int duration, int amplitude);
    short[] generate(double[] freq, int duration, int amplitude);
    short[] generate(double[] freq, int duration, int amplitude, ADSR env);
    short[] generate(double[] freq, int duration, int amplitude, double modFactor);
    short[] generate(double[] freq, int duration, int amplitude, ADSR env, double modFactor);
    short[] generate(double[] freq, int duration, int amplitude, ADSR env, double modFactor, ADSR modEnv);

}