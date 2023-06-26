package dhbw.si.audio.synth.envelopes;

/**
 * @author B. Frahm
 */
public class OneEnvelope implements Envelope {
    @Override
    public double getAmplitudeFactor(int pos) {
        return 1;
    }

    @Override
    public void setSectionLen(int envLen) {

    }

}