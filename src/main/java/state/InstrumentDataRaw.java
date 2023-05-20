package state;

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

    // Reverb parameters
    double[] delayReverb;
    double[] feedbackReverb;

    // Filter parameters
    double[] frequency;
    boolean highPass;

    // Panning
    double[] pan;
}
