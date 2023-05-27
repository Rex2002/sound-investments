package audio.harmonizer;

import audio.Constants;
import state.InstrumentDataRaw;
import audio.synth.InstrumentData;
import audio.synth.fx.FilterData;

import java.util.Arrays;
import java.util.Random;

public class Harmonizer {
    private final InstrumentDataRaw dataRaw;

    public Harmonizer(InstrumentDataRaw dataRaw) {
        this.dataRaw = dataRaw;
    }

    public InstrumentData harmonize() {
        InstrumentData data = new InstrumentData();
        data.setPitch(normalizePitch(dataRaw.getPitch()));
        data.setVolume(normalizeVolume(dataRaw.getRelVolume(), dataRaw.getAbsVolume()));

        data.setDelayEcho(normalizeDelayEcho(dataRaw.getDelayEcho()));
        data.setFeedbackEcho(normalizeFeedbackEcho(dataRaw.getFeedbackEcho()));
        data.setDelayReverb(normalizeDelayReverb(dataRaw.getDelayReverb()));
        data.setFeedbackReverb(normalizeFeedbackReverb(dataRaw.getFeedbackReverb()));

        data.setFilterData(normalizeFilter(dataRaw.getFrequency(), dataRaw.isHighPass()));
        data.setPan(normalizePan(dataRaw.getPan()));

        data.setInstrument(dataRaw.getInstrument());

        return data;
    }
    private int[] normalizePitch(double[] pitch) {
        double[] scale = getRandomScale();

        int[] output = new int[pitch.length];
        for (int i = 0; i < pitch.length; i++) {
            double sum = 0.0;
            for (int offsetIndex = 0; offsetIndex < scale.length; offsetIndex++) {
                sum += scale[offsetIndex];
                if (pitch[i] <= sum) {
                    output[i] = 36 + offsetIndex;
                }
            }
        }
        return output;
    }
    private double[] getRandomScale() {
        double[] scale = new double[]{
                2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f,
                2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f,
                2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f,
                2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f, 1/76f, 2/76f
        };

        int shift = new Random().nextInt(12);
        double[] buffer = Arrays.copyOfRange(scale, 0, shift);
        for (int i = 0; i < scale.length; i++) {
            scale[i] = i < scale.length - shift ? scale[i + shift] : buffer[shift - (scale.length -i)];
        }

        return scale;
    }

    private double[] normalizeVolume(double[] relVolume, boolean[] absVolume) {
        if (relVolume == null && absVolume == null) {
            return new double[]{1.0};
        }
        else if (relVolume == null) {
            double[] volume = new double[absVolume.length];
            for (int i = 0; i < absVolume.length; i++) {
                volume[i] = absVolume[i]?1.0:0.0;
            }
            return volume;
        }
        else {
            for (int i = 0; i < relVolume.length; i++) {
                if (relVolume[i] > 1 || relVolume[i] < 0) {
                    throw new RuntimeException("relVolume data is non-compliant at index " + i);
                }
                if (absVolume != null && absVolume.length > i) {
                    relVolume[i] = absVolume[i] ? relVolume[i] : 0.0;
                }
            }
            return relVolume;
        }
    }

    private int[] normalizeDelayEcho(double[] delayEcho) {
        //TODO: test delay times
        double[] delays = new double[]{4/96f, 6/96f, 8/96f, 12/96f, 16/96f, 24/96f, 32/96f,48/96f, 1f };
        int[] output = new int[delayEcho.length];
        for (int i = 0; i < delayEcho.length; i++) {

            output[i] = (int) delays[(int) (delayEcho[i] * delays.length)] * (Constants.SAMPLE_RATE * 60 / (Constants.TEMPO * 4));
        }
        return output;
    }

    private double[] normalizeFeedbackEcho(double[] feedbackEcho) {
        //TODO: test values
        for (int i = 0; i < feedbackEcho.length; i++) {
            feedbackEcho[i] *= 0.9;
        }
        return feedbackEcho;
    }

    private int[] normalizeDelayReverb(double[] delayReverb) {
        int[] output = new int[delayReverb.length];
        for (int i = 0; i < delayReverb.length; i++) {
            output[i] = (int) (delayReverb[i] * 2205);
        }
        return output;
    }

    private double[] normalizeFeedbackReverb(double[] feedbackReverb) {
        //TODO: test values
        for (int i = 0; i < feedbackReverb.length; i++) {
            feedbackReverb[i] *= 0.8;
        }
        return feedbackReverb;
    }

    private FilterData normalizeFilter(double[] cutoff, boolean highPass) {
        FilterData filter = new FilterData();

        for (int i = 0; i < cutoff.length; i++) {
            cutoff[i] = 40 + cutoff[i] * (20000 - 40);
        }

        filter.setCutoff(cutoff);
        filter.setBandwidth(new double[]{0.5});
        filter.setHighPass(highPass);

        return filter;
    }

    private double[] normalizePan(double[] pan) {
        for (int i = 0; i < pan.length; i++) {
            pan[i] = (pan[i] * 2) -1;
        }
        return pan;
    }
}