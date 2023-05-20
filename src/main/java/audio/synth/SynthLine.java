package audio.synth;

import lombok.Data;
import audio.synth.envelopes.ADSR;
import audio.synth.envelopes.Envelope;
import audio.synth.fx.Effect;
import audio.synth.generators.SineWaveGenerator;
import audio.synth.generators.WaveGenerator;

import static audio.synth.Test.CHANNEL_NO;
import static audio.synth.Test.SAMPLE_RATE;

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
        out = new short[length * SAMPLE_RATE * CHANNEL_NO];
        for(int i = 0; i < data.volume.length; i++){
            out[i] = (short) (Short.MAX_VALUE * data.volume[i]);
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
        if(data.feedbackEcho != null && data.delayEcho != null) {
            out = Effect.echo(out, data.feedbackEcho, data.delayEcho);
        }
    }

    private void applyReverb(){
        if(data.feedbackReverb != null && data.delayReverb != null) {
            out = Effect.echo(out, data.feedbackReverb, data.delayReverb);
        }
    }

    private void applyFilter(){
        Effect.IIR(out, data.getFilterData());
    }

    private void applyPan(){
        for(int pos = 0; pos < out.length; pos+=2){
            double panValue = data.getPan()[(int) (((double) pos / out.length) * data.getPan().length)];
            if(panValue < 0){
                out[pos] = (short) (out[pos] * (1- panValue) * -1);
            }
            else if(panValue > 0){
                out[pos + 1] = (short) (out[pos + 1] * (1- panValue));
            }
        }
    }

    private double[] transformNotesToFreq(){
        double[] tPitch = new double[data.pitch.length];
        for( int i = 0; i < data.getPitch().length; i++){
            tPitch[i] = getFreqFromRelValue(data.getPitch()[i]);
        }
        return tPitch;
    }

    private double getFreqFromRelValue(int rel){
        return Math.pow(2, ((double)rel-69)/12) * 440;
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