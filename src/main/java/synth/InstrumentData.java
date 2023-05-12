package synth;

import lombok.Data;

@Data
public class InstrumentData {
    // Volume
    double[] volume;

    // Played notes
    int[] pitch;

    // Timbre
    InstrumentEnum instrument;

    // Echo parameters
    int[] delayEcho;
    double[] feedbackEcho;

    // Reverb parameters
    int[] delayReverb;
    double[] feedbackReverb;

    // Filter parameters
    int[] cutoff;
    double[] order;
    boolean highPass;

    // Panning
    double[] pan;
}