package audio.synth;

import audio.synth.fx.FilterData;

public class GlobalFxParam {
    int[] delayReverb;
    double[] feedbackReverb;
    FilterData filterData;

    public int[] getDelayReverb() {
        return delayReverb;
    }

    public void setDelayReverb(int[] delayReverb) {
        this.delayReverb = delayReverb;
    }

    public double[] getFeedbackReverb() {
        return feedbackReverb;
    }

    public void setFeedbackReverb(double[] feedbackReverb) {
        this.feedbackReverb = feedbackReverb;
    }

    public FilterData getFilterData() {
        return filterData;
    }

    public void setFilterData(FilterData filterData) {
        this.filterData = filterData;
    }
}