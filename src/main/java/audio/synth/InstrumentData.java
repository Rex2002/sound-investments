package audio.synth;

import lombok.Data;
import audio.synth.fx.FilterData;

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
    FilterData filterData;
    // Panning
    double[] pan;
}