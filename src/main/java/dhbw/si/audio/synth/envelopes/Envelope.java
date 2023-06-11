package dhbw.si.audio.synth.envelopes;

public interface Envelope {
    double getAmplitudeFactor(int pos);

    void setSectionLen(int envLen);
}