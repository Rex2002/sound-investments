package dhbw.si.audio.synth;

import dhbw.si.audio.synth.fx.FilterData;
import lombok.Data;

@Data
public class GlobalFxData {
    int[] delayReverb;
    double[] feedbackReverb;
    FilterData filterData;
}