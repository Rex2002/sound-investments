package audio.synth;

import audio.Util;
import audio.synth.envelopes.ADSR;
import audio.synth.envelopes.Envelope;
import audio.synth.fx.Effect;
import audio.synth.fx.FilterData;
import audio.synth.generators.SineWaveGenerator;
import lombok.Data;

import static audio.Constants.CHANNEL_NO;
import static audio.Constants.SAMPLE_RATE;

public class SynthLine {

    @Data
    static class UnpackedInstr {
        private double modFactor;
        private Envelope env;
        private Envelope modEnv;
    }

    final InstrumentData data;
    double[] out;
    final int sampleNumber;

    /**
     * puts data to the data-field and calculates the number of samples for this line based on length, sample rate and number of channels.
     * @param data object that hold all necessary information for sound creation
     * @param length length in seconds
     */
    public SynthLine(InstrumentData data, double length) {
        this.data = data;
        this.sampleNumber = (int) (length * SAMPLE_RATE * CHANNEL_NO);
    }

    /**
     * method to orchestrate sound generation pipeline based on this.data (set in the constructor)
     * @return an array that contains sound-data and can be passed to the playbackController or the mixer
     */
    public double[] synthesize() {
        this.applyVolume();
        this.applyTimbre();
        this.applyEcho();
        this.applyReverb();
        this.applyFilter();
        this.applyPan();
        return out;
    }

    /**
     * for each sample the volume is determined by stretching data.volume to the necessary length.
     * Afterward a highpass filter is used to remove all steep changes in the volume (which create a cracking-sound)
     */
    private void applyVolume() {
        out = new double[sampleNumber];
        for (int i = 0; i < out.length; i++) {
            out[i] = 32767.0 * data.volume[Util.getRelPosition(i, out.length, data.volume.length)];
        }
        FilterData filter = new FilterData();
        filter.setHighPass(false);
        filter.setCutoff(new double[]{200});
        filter.setBandwidth(new double[]{.1});
        out = Effect.IIR(out, filter);
    }

    /**
     * this method transforms the volume array to an array that contains actual, meaningful sound-data.
     * Depending on the instrument that was passed, the correct settings for modulation and envelopes for the sound-wave-generation are set.
     * Also, the notes are transformed from midi-notes to actual frequencies.
     */
    private void applyTimbre() {
        UnpackedInstr instr = this.unpackInstrument();
        double[] transformedPitch = this.transformNotesToFreq();
        SineWaveGenerator gen = new SineWaveGenerator();
        out = gen.generate(transformedPitch, sampleNumber, out, instr.env, instr.getModFactor(), instr.modEnv);
    }

    private void applyEcho() {
        if (data.feedbackEcho != null && data.delayEcho != null) {
            out = Effect.echoWithFeedback(out, data.feedbackEcho, data.delayEcho);
        }
    }

    private void applyReverb() {
        if (data.feedbackReverb != null && data.delayReverb != null) {
            out = Effect.echoWithFeedback(out, data.feedbackReverb, data.delayReverb);
        }
    }

    private void applyFilter() {
        if (data.getFilterData() != null) {
            System.out.println("we are filtering >party<");
            out = Effect.IIR(out, data.getFilterData());
        }
    }

    private void applyPan() {
        for (int pos = 0; pos < out.length - 1; pos += 2) {
            double panValue = data.getPan()[Util.getRelPosition(pos, out.length, data.getPan().length)];
            if (panValue < 0) {
                out[pos + 1] *= (1 + panValue);
            } else if (panValue > 0) {
                out[pos] *= (1 - panValue);
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
            case RETRO_SYNTH -> {
                uInstr.setModFactor(1.5);
                uInstr.env = new ADSR(.1, .3, .5, .2);
                uInstr.modEnv = new ADSR(.1, .3, .5, .2);
            }
            case STRINGS_SYNTH -> {
                uInstr.setModFactor(0.2142070863);
                uInstr.env = new ADSR(.25, .15, .5, .14);
                uInstr.modEnv = new ADSR(.15, .14, .5, .16);
            }
            case BANJO_SYNTH -> {
                uInstr.setModFactor(0.4715820051);
                uInstr.env = new ADSR(.03, .4, .25, .4);
                uInstr.modEnv = new ADSR(.15, .14, .5, .16);
            }
            case BRASS_SYNTH -> {
                uInstr.setModFactor(0.5);
                uInstr.env = new ADSR(.15, .2, .55, .1);
                uInstr.modEnv = new ADSR(.15, .2, .55, .1);
            }
        }

        return uInstr;
    }
}