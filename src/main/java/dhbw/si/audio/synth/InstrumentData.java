package dhbw.si.audio.synth;

import lombok.Data;
import dhbw.si.audio.synth.fx.FilterData;

@Data
public class InstrumentData {
    // Volume
    double[] volume;

    // Played notes
    int[] pitch;

    // Timbre
    InstrumentEnum instrument = null;

    // Echo parameters
    int[] delayEcho = null;
    double[] feedbackEcho = null;

    // Reverb parameters
    int[] delayReverb = null;
    double[] feedbackReverb = null;

    // Filter parameters
    FilterData filterData = null;
    // Panning
    double[] pan = null;
}