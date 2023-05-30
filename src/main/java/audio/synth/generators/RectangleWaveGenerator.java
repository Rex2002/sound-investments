package audio.synth.generators;

import audio.synth.envelopes.Envelope;


import static audio.Constants.CHANNEL_NO;
import static audio.Constants.SAMPLE_RATE;


// TODO work in progress -> figure out whether this is acutally needed or if frequency modualtion of sines is the way to go.
@Deprecated
public class RectangleWaveGenerator{


    public short[] generate(double freq, int duration, int amplitude) {
        return new short[0];
    }


    public short[] generate(double[] freq, int duration, int amplitude){
        short[] sq = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for(int i = 0; i < sq.length; i += 2){
            PhaseAdvancers.advancePhaseSquare(phases, freq[(int) (((double) i / sq.length) * freq.length)]);
            sq[i] = (short) (phases.ret * amplitude);
            sq[i+1] = (short) (phases.ret * amplitude);
        }
        return sq;
    }

    public short[] generate(double[] freq, int duration, int amplitude, Envelope env){
        short[] sq = new short[duration * SAMPLE_RATE * CHANNEL_NO];
        env.setSectionLen(sq.length/freq.length);
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for(int i = 0; i < sq.length-1; i += 2){
            PhaseAdvancers.advancePhaseSquare(phases, freq[(int) (((double) i / sq.length) * freq.length)]);
            sq[i] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
            sq[i+1] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));

        }
        return sq;
    }


    public short[] generate(double[] freq, int duration, int amplitude, double modFactor) {
        return new short[0];
    }

    public short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor) {
        return new short[0];
    }

    public short[] generate(double[] freq, int duration, int amplitude, Envelope env, double modFactor, Envelope modEnv) {
        return new short[0];
    }
}