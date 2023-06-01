package audio.synth;

import audio.Util;
import audio.mixer.SampleLoader;

import static audio.Constants.*;

public class EvInstrLine {
    EvInstrData data;
    int length;
    public EvInstrLine(EvInstrData data, int length){
        this.data = data;
        this.length = length;
    }

    public double[] synthesize(){
        double[] out = new double[length * SAMPLE_RATE * CHANNEL_NO];
        double[] sample = SampleLoader.loadEventSample(data.type.toFileName());


        int samplesPer16th = 60 / (TEMPO * 4) * SAMPLE_RATE * CHANNEL_NO;
        int lastCopy = -samplesPer16th;

        for(int i = 0; i < out.length; i++){
            if(data.values[Util.getRelPosition(i,out.length,data.values.length)] && lastCopy + samplesPer16th < i) {
                lastCopy = i;
                System.arraycopy(sample, 0, out, i, sample.length);
            }
        }

        return out;
    }
}