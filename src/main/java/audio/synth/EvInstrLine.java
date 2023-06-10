package audio.synth;

import app.AppError;
import audio.Util;
import audio.mixer.SampleLoader;

import static audio.Constants.*;

public class EvInstrLine {
    final EvInstrData data;
    final int sampleNumber;

    public EvInstrLine(EvInstrData data, double length){
        this.data = data;
        this.sampleNumber = (int) (length * SAMPLE_RATE * CHANNEL_NO);
    }

    /**
     * this method puts the specified sample into an array at the relatively same positions as the true values in the data.values array.<br/>
     * @return an array containing the evInstr sample at the positions specified by the data.values
     */
    public double[] synthesize() throws AppError {
        double[] out = new double[sampleNumber];
        double[] sample = SampleLoader.loadEventSample(data.type.toFileName());


        int samplesPer16th = (int) (60 / ((double)TEMPO * 4) * SAMPLE_RATE * CHANNEL_NO);
        int lastCopy = - 2* samplesPer16th;

        for(int i = 0; i < out.length; i+=samplesPer16th){
            if(data.values[Util.getRelPosition(i,out.length,data.values.length)] && lastCopy + 2 * samplesPer16th < i) {
                lastCopy = i;
                if(i + sample.length < out.length)
                    System.arraycopy(sample, 0, out, i, sample.length);
                //else System.arraycopy(sample, 0, out, i, sample.length - ());
            }
        }

        return out;
    }
}