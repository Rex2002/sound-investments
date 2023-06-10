package app.mapping;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GlobalFxDataRaw {
    double[] delayReverb;
    double[] feedbackReverb;
    double[] cutOffFrequency;
    boolean[] onOffReverb;
    boolean[] onOffFilter;
    boolean highPass;

    public GlobalFxDataRaw(double[] delayReverb, double[] feedbackReverb, double[] cutOffFrequency, boolean[] onOffReverb, boolean[] onOffFilter, boolean highPass) {
        this.delayReverb = delayReverb;
        this.feedbackReverb = feedbackReverb;
        this.cutOffFrequency = cutOffFrequency;
        this.onOffReverb = onOffReverb;
        this.onOffFilter = onOffFilter;
        this.highPass = highPass;
    }
}