package dhbw.si.app.mapping;

import lombok.Data;
import dhbw.si.audio.synth.InstrumentEnum;

@Data
public class InstrumentDataRaw {
    // Volume
    public double[] relVolume;
    public boolean[] absVolume;

    // Pitch
    public double[] pitch;

    // Timbre
    public InstrumentEnum instrument;

    // Echo parameters
    public double[] delayEcho;
    public double[] feedbackEcho;
    public boolean[] onOffEcho;

    // Reverb parameters
    public double[] delayReverb;
    public double[] feedbackReverb;
    public boolean[] onOffReverb;

    // Filter parameters
    public double[] frequency;
    public boolean highPass;
    public boolean[] onOffFilter;

    // Panning
    public double[] pan;

    public InstrumentDataRaw(double[] relVolume, boolean[] absVolume, double[] pitch, InstrumentEnum instrument,
            double[] delayEcho, double[] feedbackEcho, boolean[] onOffEcho, double[] delayReverb,
            double[] feedbackReverb, boolean[] onOffReverb, double[] frequency, boolean highPass, boolean[] onOffFilter,
            double[] pan) {
        this.relVolume = relVolume;
        this.absVolume = absVolume;
        this.pitch = pitch;
        this.instrument = instrument;
        this.delayEcho = delayEcho;
        this.feedbackEcho = feedbackEcho;
        this.onOffEcho = onOffEcho;
        this.delayReverb = delayReverb;
        this.feedbackReverb = feedbackReverb;
        this.onOffReverb = onOffReverb;
        this.frequency = frequency;
        this.highPass = highPass;
        this.onOffFilter = onOffFilter;
        this.pan = pan;
    }
}
