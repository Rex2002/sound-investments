package dhbw.si.audio.synth.envelopes;

/**
 * @author B. Frahm
 */
public interface Envelope {
    double getAmplitudeFactor(int pos);

    void setSectionLen(int envLen);
}