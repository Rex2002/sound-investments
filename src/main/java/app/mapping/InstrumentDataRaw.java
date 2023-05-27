package app.mapping;

import lombok.Data;
import audio.synth.InstrumentEnum;

@Data
public class InstrumentDataRaw {
    // Volume
    double[] relVolume;
    boolean[] absVolume;

    // Pitch
    double[] pitch;

    // Timbre
    InstrumentEnum instrument;

    // Echo parameters
    double[] delayEcho;
    double[] feedbackEcho;
    boolean[] onOffEcho;

    // Reverb parameters
    double[] delayReverb;
    double[] feedbackReverb;
    boolean[] onOffReverb;

    // Filter parameters
    double[] frequency;
    boolean highPass;
    boolean[] onOffFilter;

    // Panning
    double[] pan;
}
