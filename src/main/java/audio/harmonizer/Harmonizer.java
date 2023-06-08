package audio.harmonizer;

import app.AppError;
import app.mapping.InstrumentDataRaw;
import audio.Constants;
import audio.Util;
import audio.synth.InstrumentData;
import audio.synth.fx.FilterData;

import java.util.Arrays;
import java.util.Random;

public class Harmonizer {
    private final InstrumentDataRaw dataRaw;
    private final int numberBeats;

    public Harmonizer(InstrumentDataRaw dataRaw, int numberBeats) {
        this.dataRaw = dataRaw;
        this.numberBeats = numberBeats;
    }

    public InstrumentData harmonize() throws AppError {
        InstrumentData data = new InstrumentData();
        data.setInstrument(dataRaw.getInstrument());

        data.setPitch(normalizePitch());
        data.setVolume(normalizeVolume());

        if (!(dataRaw.getDelayEcho() == null && dataRaw.getFeedbackEcho() == null && dataRaw.getOnOffEcho() == null)) {
            data.setDelayEcho(normalizeDelayEcho());
            data.setFeedbackEcho(normalizeFeedbackEcho());
        }

        if (!(dataRaw.getDelayReverb() == null && dataRaw.getFeedbackReverb() == null
                && dataRaw.getOnOffReverb() == null)) {
            data.setDelayReverb(normalizeDelayReverb());

            data.setFeedbackReverb(normalizeFeedbackReverb());
        }

        if (!(dataRaw.getFrequency() == null && dataRaw.getOnOffFilter() == null)) {
            data.setFilterData(normalizeFilter());
        }

        data.setPan(normalizePan(dataRaw.getPan()));

        return data;
    }

    private int[] normalizePitch() throws AppError {
        int NUMBER_OCTAVES = 4;
        int FIRST_NOTE = 36;

        int[] scale = getRandomScale();

        double[] pitch = quantizePitch();

        int[] output = new int[pitch.length];
        for (int i = 0; i < pitch.length; i++) {
            checkDouble(pitch[i], "pitch", i);

            double sum = 0.0;
            for (int offsetIndex = 0; offsetIndex < scale.length * NUMBER_OCTAVES; offsetIndex++) {
                sum += scale[offsetIndex % scale.length];
                if (pitch[i] <= sum / (scale.length * NUMBER_OCTAVES) ) {
                    output[i] = FIRST_NOTE + offsetIndex;
                    break;
                }
            }
        }
        return output;
    }

    private int[] getRandomScale() {
        int[] scale = new int[] {
                2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2
        };

        int shift = new Random().nextInt(scale.length);
        if (shift > 0) {
            int[] buffer = Arrays.copyOfRange(scale, 0, shift);
            for (int i = 0; i < scale.length; i++) {
                scale[i] = i < scale.length - shift ? scale[i + shift] : buffer[shift - (scale.length - i)];
            }
        }

        return scale;
    }

    /**
     * @return data array that has the exact length where one data point can be
     *         sonified as one quarter note
     *         compresses long data array to the required length by averaging a
     *         number of data points into one note.
     */
    private double[] quantizePitch() {
        double[] pitch = dataRaw.getPitch();
        double[] notes = new double[numberBeats];
        int bufferLength = pitch.length / numberBeats;
        if (bufferLength > 0) {
            for (int i = 0, bufferStart = 0; i < notes.length; i++, bufferStart += bufferLength) {
                notes[i] = 0;
                if(pitch[bufferStart] == -1 || pitch[bufferStart + bufferLength - 1] == -1){
                    notes[i] = -1;
                    continue;
                }
                for (int j = bufferStart; j < bufferStart + bufferLength; j++) {
                    notes[i] += pitch[j] / bufferLength;
                }
            }
        }
        else{
            for(int i = 0; i < notes.length; i++){
                notes[i] = pitch[Util.getRelPosition(i, notes.length, pitch.length)];
            }
        }

        return notes;
    }

    private double[] normalizeVolume() throws AppError {
        double MAX_VOLUME = 1.0;
        double MUTE_VOLUME = 0.0;
        double[] relVolume = dataRaw.getRelVolume();
        boolean[] absVolume = dataRaw.getAbsVolume();
        double[] pitch = dataRaw.getPitch();
        if (relVolume == null && absVolume == null) {
            double[] volume = new double[pitch.length];
            for(int i = 0; i < volume.length; i ++){
                volume[i] = pitch[i] == -1 ? MUTE_VOLUME : MAX_VOLUME;
            }
            return volume;
        } else if (relVolume == null) {
            double[] volume = new double[absVolume.length];
            for (int i = 0; i < absVolume.length; i++) {
                volume[i] = absVolume[i] && pitch[i] != -1 ? MAX_VOLUME : MUTE_VOLUME;
            }
            return volume;
        } else {
            for (int i = 0; i < relVolume.length; i++) {
                checkDouble(relVolume[i], "relVolume", i);

                if (absVolume != null && absVolume.length > i) {
                    relVolume[i] = absVolume[i] && pitch[i] != -1 && relVolume[i] != -1? relVolume[i] : MUTE_VOLUME;
                }
            }
            return relVolume;
        }
    }

    private int[] normalizeDelayEcho() throws AppError {
        int SAMPLES_PER_BAR = Constants.SAMPLE_RATE * 60 / (Constants.TEMPO / 4);
        double[] delayEcho = dataRaw.getDelayEcho();

        // TODO: test delay times
        if (delayEcho != null) {
            double[] delays = new double[] { 1/24f, 1/16f, 1/12f, 1/8f, 1/6f, 1/4f, 1/3f, 1/2f, 1 };
            int[] output = new int[delayEcho.length];

            for (int i = 0; i < delayEcho.length; i++) {
                checkDouble(delayEcho[i], "delayEcho", i);

                output[i] = (int) ( delays[(int) (delayEcho[i] * (delays.length - 1) )] * SAMPLES_PER_BAR );
            }
            return output;
        } else {
            return new int[] { (int) (1/8f * SAMPLES_PER_BAR) };
        }
    }

    private double[] normalizeFeedbackEcho() throws AppError {
        double MAX_FEEDBACK_ECHO = 0.9;
        double DEFAULT_FEEDBACK_ECHO = 0.7;
        double MUTE_ECHO = 0;
        double[] feedbackEcho = dataRaw.getFeedbackEcho();
        double[] delayEcho = dataRaw.getDelayEcho();
        boolean[] onOffEcho = dataRaw.getOnOffEcho();

        // TODO: test values
        if (feedbackEcho != null) {
            for (int i = 0; i < feedbackEcho.length; i++) {
                checkDouble(feedbackEcho[i], "feedbackEcho", i);

                if ((onOffEcho != null && !onOffEcho[i]) || (delayEcho != null && delayEcho[i] == -1)) {
                    feedbackEcho[i] = MUTE_ECHO;
                } else {
                    feedbackEcho[i] *= MAX_FEEDBACK_ECHO;
                }
            }
        } else if (onOffEcho != null) {
            feedbackEcho = new double[onOffEcho.length];
            for (int i = 0; i < onOffEcho.length; i++) {
                feedbackEcho[i] = onOffEcho[i] && delayEcho[i] != -1 ? DEFAULT_FEEDBACK_ECHO : MUTE_ECHO;
            }
        } else if (delayEcho != null){
            feedbackEcho = new double[delayEcho.length];
            for(int i = 0; i < delayEcho.length; i++){
                feedbackEcho[i] = delayEcho[i] == -1 ? MUTE_ECHO : DEFAULT_FEEDBACK_ECHO;
            }
        }
        else{
            feedbackEcho = new double[]{DEFAULT_FEEDBACK_ECHO};
        }
        return feedbackEcho;
    }

    private int[] normalizeDelayReverb() throws AppError {
        int MAX_DELAY_REVERB = Constants.SAMPLE_RATE / 20;    // number of samples corresponding to 50ms
        int DEFAULT_DELAY_REVERB = Constants.SAMPLE_RATE / 25; // number of samples corresponding to 40ms
        double[] delayReverb = dataRaw.getDelayReverb();
        if (delayReverb != null) {
            int[] output = new int[delayReverb.length];
            for (int i = 0; i < delayReverb.length; i++) {
                checkDouble(delayReverb[i], "delayReverb", i);

                output[i] = (int) (delayReverb[i] * MAX_DELAY_REVERB);
            }
            return output;
        } else {
            // TODO: test value
            return new int[] { DEFAULT_DELAY_REVERB };
        }
    }

    private double[] normalizeFeedbackReverb() throws AppError {
        double MAX_FEEDBACK_REVERB = 0.8;
        double DEFAULT_FEEDBACK_REVERB = 0.6;
        double MUTE_REVERB = 0;
        double[] feedbackReverb = dataRaw.getFeedbackReverb();
        double[] delayReverb = dataRaw.getDelayReverb();
        boolean[] onOffReverb = dataRaw.getOnOffReverb();
        // TODO: test values
        if (feedbackReverb != null) {
            for (int i = 0; i < feedbackReverb.length; i++) {
                checkDouble(feedbackReverb[i], "feedbackReverb", i);

                if ((onOffReverb != null && !onOffReverb[i]) || (delayReverb != null && delayReverb[i] == -1)) {
                    feedbackReverb[i] = MUTE_REVERB;
                } else {
                    feedbackReverb[i] *= MAX_FEEDBACK_REVERB;
                }
            }
        } else if (onOffReverb != null) {
            feedbackReverb = new double[onOffReverb.length];
            for (int i = 0; i < onOffReverb.length; i++) {
                feedbackReverb[i] = onOffReverb[i] && delayReverb[i] != -1 ? DEFAULT_FEEDBACK_REVERB : MUTE_REVERB;
            }
        } else if (delayReverb != null) {
            feedbackReverb = new double[delayReverb.length];
            for(int i = 0; i < delayReverb.length; i++){
                feedbackReverb[i] = delayReverb[i] == -1 ? MUTE_REVERB : DEFAULT_FEEDBACK_REVERB;
            }
        }
        else{
            feedbackReverb = new double[]{DEFAULT_FEEDBACK_REVERB};
        }
        return feedbackReverb;
    }

    private FilterData normalizeFilter() throws AppError {
        double BANDWIDTH = 0.5;
        int MIN_FREQ = 40;
        int MAX_FREQ = 20000;
        int HIGH_PASS_OFF = 1;
        int HIGH_PASS_DEFAULT_FREQ = 1000;
        int LOW_PASS_OFF = 50000;
        int LOW_PASS_DEFAULT_FREQ = 500;
        double[] cutoff = dataRaw.getFrequency();
        boolean[] onOff = dataRaw.getOnOffFilter();
        boolean highPass = dataRaw.isHighPass();
        FilterData filter = new FilterData();

        if (cutoff != null) {
            for (int i = 0; i < cutoff.length; i++) {
                checkDouble(cutoff[i], "cutoff", i);

                if ((onOff != null && !onOff[i]) || cutoff[i] == -1) {
                    cutoff[i] = highPass ? HIGH_PASS_OFF : LOW_PASS_OFF;
                } else {
                    cutoff[i] = MIN_FREQ + cutoff[i] * (MAX_FREQ - MIN_FREQ);
                }
            }
        } else {
            cutoff = new double[onOff.length];
            for (int i = 0; i < onOff.length; i++) {
                cutoff[i] = highPass ? HIGH_PASS_DEFAULT_FREQ : LOW_PASS_DEFAULT_FREQ;
            }
        }

        filter.setCutoff(cutoff);
        filter.setBandwidth(new double[] { BANDWIDTH });
        filter.setHighPass(highPass);
        return filter;
    }

    private double[] normalizePan(double[] pan) throws AppError {
        if (pan != null) {
            for (int i = 0; i < pan.length; i++) {
                checkDouble(pan[i], "pan", i);

                pan[i] = pan[i] == -1 ? 0 : pan[i] * 2 - 1;
            }
            return pan;
        } else {
            return new double[] { 0.0 };
        }
    }

    private void checkDouble(double value, String collection, int index) throws AppError {
        // -1 is needed, because that is the internal "off"/switch
        if ((value > 1 || value < 0) &&  value != -1) {
            throw new AppError("Mapped Data non-compliant: " + collection + "[" + index +
                    "] not in range(0,1) with value " + value);
        }
    }
}