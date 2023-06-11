package dhbw.si.audio.events;

import dhbw.si.app.AppError;
import dhbw.si.audio.Util;
import dhbw.si.audio.mixer.SampleLoader;

import static dhbw.si.audio.Constants.*;

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

        for (int i = 0; i < data.values.length; i++) {
            if (data.values[i]) {
                int j = Util.getRelPosition(i, data.values.length, out.length);
                if (lastCopy + 2 * samplesPer16th < j) {
                    lastCopy = j;
                    System.arraycopy(sample, 0, out, j, sample.length);
                }
            }
        }

        return out;
    }
}