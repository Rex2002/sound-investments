package synth;

import lombok.Data;
import synth.envelopes.ADSR;
import synth.envelopes.Envelope;
import synth.generators.SineWaveGenerator;
import synth.generators.WaveGenerator;

import static synth.Test.CHANNEL_NO;
import static synth.Test.SAMPLE_RATE;

public class SynthLine {

    enum WaveTypes {
        SINE,
        SQUARE,
        SAWTOOTH
    }

    @Data
    static class UnpackedInstr{
        private WaveTypes waveType;
        private double modFactor;
        private double[] envFactors;
        private double[] modEnvFactors;
    }

    InstrumentData data;
    short[] out;
    int length;
    public SynthLine(InstrumentData data, int length){
        this.data = data;
        out = new short[length * SAMPLE_RATE * CHANNEL_NO];
        this.length = length;
    }

    public short[] synthesize(){
        this.applyVolume();
        this.applyTimbre();
        this.applyEcho();
        this.applyReverb();
        this.applyFilter();
        this.applyPan();
        return out;
    }

    private void applyVolume(){
        for(int i = 0; i < length * SAMPLE_RATE; i++){
            out[2*i] = (short) (Short.MAX_VALUE * data.volume[i]);
            out[2*i+1] = (short) (Short.MAX_VALUE * data.volume[i]);
        }
    }

    private void applyTimbre(){
        UnpackedInstr instr = this.unpackInstrument();
        double[] transformedPitch = this.transformNotesToFreq();

        switch (instr.waveType) {
            case SINE -> {
                WaveGenerator gen = new SineWaveGenerator();
                Envelope modEnv = new ADSR(instr.getModEnvFactors()[0], instr.getModEnvFactors()[1], instr.getModEnvFactors()[2], instr.getModEnvFactors()[3]);
                Envelope env = new ADSR(instr.getEnvFactors()[0], instr.getEnvFactors()[1], instr.getEnvFactors()[2], instr.getEnvFactors()[3]);
                out = gen.generate(transformedPitch, length, out, env, instr.getModFactor(), modEnv);
            }
            case SQUARE, SAWTOOTH -> throw new RuntimeException("implement Sawtooth, square");
        }
    }

    private void applyEcho(){
        out = Effect.echo(out, data.feedbackEcho, data.delayEcho);
    }

    private void applyReverb(){
        out = Effect.echo(out, data.feedbackReverb, data.delayReverb);
    }

    private void applyFilter(){
        // TODO (Issue #29)
    }

    private void applyPan(){
        for(int pos = 0; pos < out.length; pos+=2){
            if(data.getPan()[pos/2] < 0){
                out[pos] = (short) (out[pos] * data.getPan()[pos / 2] * -1);
            }
            else if(data.getPan()[pos/2] > 0){
                out[pos + 1] = (short) (out[pos + 1] * data.getPan()[pos / 2]);
            }
        }
    }

    private double[] transformNotesToFreq(){
        double[] tPitch = new double[data.pitch.length];
        for( int i = 0; i < data.pitch.length; i++){
            tPitch[i] = getFreqFromRelValue(data.pitch[i]);
        }
        return tPitch;
    }

    private double getFreqFromRelValue(int rel){
        // TODO implement mapping of rel note number (0-47) to according frequencies.
        return 440;
    }


    private UnpackedInstr unpackInstrument(){

        UnpackedInstr uInstr = new UnpackedInstr();
        switch (this.data.getInstrument()){
            case SYNTH_ONE -> {
                uInstr.setWaveType(WaveTypes.SINE);
                uInstr.setModFactor(1.5);
                uInstr.setEnvFactors(new double[]{0.1,0.3, 0.5, 0.2});
                uInstr.setModEnvFactors(new double[]{0.1,0.3, 0.5, 0.2});
            }
            case SYNTH_TWO -> throw new RuntimeException("implement instruments");
        }

        return uInstr;
    }
}