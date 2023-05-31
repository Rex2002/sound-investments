package audio.synth.generators;

import audio.Constants;
import audio.synth.envelopes.ADSR;

@Deprecated
public class SawtoothWaveGenerator {
    @Deprecated
    public short[] createSawtooth(double[] freq, int duration, int amplitude) {
        short[] sw = new short[duration * Constants.SAMPLE_RATE * Constants.CHANNEL_NO];
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for (int i = 0; i < sw.length; i += 2) {
            PhaseAdvancers.advancePhaseSawtooth(phases, freq[(int) (((double) i / sw.length) * freq.length)]);
            sw[i] = (short) (phases.ret * amplitude);
            sw[i + 1] = (short) (phases.ret * amplitude);
        }
        return sw;
    }

    @Deprecated
    public short[] createSawtooth(double[] freq, int duration, int amplitude, ADSR env) {
        short[] sw = new short[duration * Constants.SAMPLE_RATE * Constants.CHANNEL_NO];
        env.setSectionLen(sw.length / freq.length);
        PhaseContainer phases = new PhaseContainer();
        phases.phase = 0;
        for (int i = 0; i < sw.length; i += 2) {
            PhaseAdvancers.advancePhaseSawtooth(phases, freq[(int) (((double) i / sw.length) * freq.length)]);

            sw[i] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
            sw[i + 1] = (short) (phases.ret * amplitude * env.getAmplitudeFactor(i));
        }
        return sw;
    }

}