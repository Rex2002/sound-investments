package audio.synth;

import audio.synth.fx.FilterData;
import lombok.Data;

@Data
public class GlobalFxData {
    int[] delayReverb;
    double[] feedbackReverb;
    FilterData filterData;
}