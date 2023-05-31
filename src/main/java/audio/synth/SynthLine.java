package audio.synth;

import audio.synth.envelopes.ADSR;
import audio.synth.envelopes.Envelope;
import audio.synth.fx.Effect;
import audio.synth.generators.SineWaveGenerator;
import lombok.Data;

import static audio.Constants.CHANNEL_NO;
import static audio.Constants.SAMPLE_RATE;

public class SynthLine {

    enum WaveTypes {
        SINE,
        SQUARE,
        SAWTOOTH
    }

    @Data
    static class UnpackedInstr {
        private WaveTypes waveType;
        private double modFactor;
        private Envelope env;
        private Envelope modEnv;
    }

    InstrumentData data;
    double[] out;
    int length;

    public SynthLine(InstrumentData data, int length) {
        this.data = data;
        this.length = length;
    }

    public double[] synthesize() {
        this.applyVolume();
        this.applyTimbre();
        this.applyEcho();
        this.applyReverb();
        this.applyFilter();
        this.applyPan();
        return out;
    }

    private void applyVolume() {
        out = new double[length * SAMPLE_RATE * CHANNEL_NO];
        for (int i = 0; i < data.volume.length; i++) {
            out[i] = Short.MAX_VALUE * data.volume[Util.getRelPosition(i, out.length, data.volume.length)];
        }
    }

    private void applyTimbre() {
        UnpackedInstr instr = this.unpackInstrument();
        double[] transformedPitch = this.transformNotesToFreq();

        switch (instr.waveType) {
            case SINE -> {
                SineWaveGenerator gen = new SineWaveGenerator();
                out = gen.generate(transformedPitch, length, out, instr.env, instr.getModFactor(), instr.modEnv);
            }
            case SQUARE, SAWTOOTH -> throw new RuntimeException("implement Sawtooth, square");
        }
    }

    private void applyEcho() {
        if (data.feedbackEcho != null && data.delayEcho != null) {
            out = Effect.echo(out, data.feedbackEcho, data.delayEcho);
        }
    }

    private void applyReverb() {
        if (data.feedbackReverb != null && data.delayReverb != null) {
            out = Effect.echo(out, data.feedbackReverb, data.delayReverb);
        }
    }

    private void applyFilter() {
        if (data.getFilterData() != null)
            Effect.IIR(out, data.getFilterData());
    }

    private void applyPan() {
        for (int pos = 0; pos < out.length; pos += 2) {
            double panValue = data.getPan()[Util.getRelPosition(pos, out.length, data.getPan().length)];
            if (panValue < 0) {
                out[pos] = out[pos] * (1 - panValue) * -1;
            } else if (panValue > 0) {
                out[pos + 1] = out[pos + 1] * (1 - panValue);
            }
        }
    }

    private double[] transformNotesToFreq() {
        double[] tPitch = new double[data.pitch.length];
        for (int i = 0; i < data.getPitch().length; i++) {
            tPitch[i] = getFreqFromRelValue(data.getPitch()[i]);
        }
        return tPitch;
    }

    private double getFreqFromRelValue(int rel) {
        return Math.pow(2, ((double) rel - 69) / 12) * 440;
    }

    private UnpackedInstr unpackInstrument() {

        UnpackedInstr uInstr = new UnpackedInstr();
        switch (this.data.getInstrument()) {
            case SYNTH_ONE -> {
                uInstr.setWaveType(WaveTypes.SINE);
                uInstr.setModFactor(1.5);
                uInstr.env = new ADSR(.1, .3, .5, .2);
                uInstr.modEnv = new ADSR(.1, .3, .5, .2);
            }
            case SYNTH_TWO -> throw new RuntimeException("implement instruments");
        }

        return uInstr;
    }
}