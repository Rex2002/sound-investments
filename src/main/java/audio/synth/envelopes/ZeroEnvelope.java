package audio.synth.envelopes;

public class ZeroEnvelope implements Envelope{
    @Override
    public double getAmplitudeFactor(int pos) {
        return 0;
    }

    @Override
    public void setSectionLen(int envLen){}
}