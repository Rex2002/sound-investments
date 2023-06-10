package app.mapping;

public class GlobalFxParamRaw {
    double[] delayReverb;
    double[] feedbackReverb;
    double[] cutOffFrequency;
    boolean[] onOffReverb;
    boolean[] onOffFilter;
    boolean highPass;

    public GlobalFxParamRaw(double[] delayReverb, double[] feedbackReverb, double[] cutOffFrequency, boolean[] onOffReverb, boolean[] onOffFilter, boolean highPass) {
        this.delayReverb = delayReverb;
        this.feedbackReverb = feedbackReverb;
        this.cutOffFrequency = cutOffFrequency;
        this.onOffReverb = onOffReverb;
        this.onOffFilter = onOffFilter;
        this.highPass = highPass;
    }

    public boolean isHighPass() {
        return highPass;
    }

    public void setHighPass(boolean highPass) {
        this.highPass = highPass;
    }

    public double[] getDelayReverb() {
        return delayReverb;
    }

    public void setDelayReverb(double[] delayReverb) {
        this.delayReverb = delayReverb;
    }

    public double[] getFeedbackReverb() {
        return feedbackReverb;
    }

    public void setFeedbackReverb(double[] feedbackReverb) {
        this.feedbackReverb = feedbackReverb;
    }

    public double[] getCutOffFrequency() {
        return cutOffFrequency;
    }

    public void setCutOffFrequency(double[] cutOffFrequency) {
        this.cutOffFrequency = cutOffFrequency;
    }

    public boolean[] getOnOffReverb() {
        return onOffReverb;
    }

    public void setOnOffReverb(boolean[] onOffReverb) {
        this.onOffReverb = onOffReverb;
    }

    public boolean[] getOnOffFilter() {
        return onOffFilter;
    }

    public void setOnOffFilter(boolean[] onOffFilter) {
        this.onOffFilter = onOffFilter;
    }

}