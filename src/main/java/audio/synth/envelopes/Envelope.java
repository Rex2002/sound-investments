package audio.synth.envelopes;

public interface Envelope {
    double getAmplitudeFactor(int pos);

    void setNoOfTones(int noOfTones);

    void setTotalLength(int tLength);

}