package audio.synth;

import audio.Util;
import audio.mixer.SampleLoader;

import static audio.Constants.*;

public class EvInstrLine {
    EvInstrData data;
    int sampleNumber;

    public EvInstrLine(EvInstrData data, double length){
        this.data = data;
        this.sampleNumber = (int) (length * SAMPLE_RATE * CHANNEL_NO);
    }

    public double[] synthesize(){
        double[] out = new double[sampleNumber];
        double[] sample = SampleLoader.loadEventSample(data.type.toFileName());


        int samplesPer16th = (int) (60 / ((double)TEMPO * 4) * SAMPLE_RATE * CHANNEL_NO);
        int lastCopy = - 2* samplesPer16th;

        for(int i = 0; i < out.length; i++){
            if(data.values[Util.getRelPosition(i,out.length,data.values.length)] && lastCopy + 2 * samplesPer16th < i) {
                lastCopy = i;
                if(i + sample.length < out.length) System.arraycopy(sample, 0, out, i, sample.length);
                //else System.arraycopy(sample, 0, out, i, sample.length - ());
            }
        }

        return out;
    }
}