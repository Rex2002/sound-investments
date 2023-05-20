package audio.synth.envelopes;

public class OneEnvelope implements Envelope{
    @Override
    public double getAmplitudeFactor(int pos) {
        return 1;
    }

    @Override
    public void setNoOfTones(int noOfTones) {}

    @Override
    public void setTotalLength(int tLength) {}
}