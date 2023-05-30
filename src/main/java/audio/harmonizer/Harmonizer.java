package audio.harmonizer;

import app.AppError;
import app.mapping.InstrumentDataRaw;
import audio.Constants;
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

        data.setPitch(normalizePitch(dataRaw.getPitch()));
        data.setVolume(normalizeVolume(dataRaw.getRelVolume(), dataRaw.getAbsVolume()));

        if (!(dataRaw.getDelayEcho() == null && dataRaw.getFeedbackEcho() == null && dataRaw.getOnOffEcho() == null)) {
            data.setDelayEcho(normalizeDelayEcho(dataRaw.getDelayEcho()));
            data.setFeedbackEcho(normalizeFeedbackEcho(dataRaw.getFeedbackEcho(), dataRaw.getOnOffEcho()));
        }

        if (!(dataRaw.getDelayReverb() == null && dataRaw.getFeedbackReverb() == null
                && dataRaw.getOnOffReverb() == null)) {
            data.setDelayReverb(normalizeDelayReverb(dataRaw.getDelayReverb()));

            data.setFeedbackReverb(normalizeFeedbackReverb(dataRaw.getFeedbackReverb(), dataRaw.getOnOffReverb()));
        }

        if (!(dataRaw.getFrequency() == null && dataRaw.getOnOffFilter() == null)) {
            data.setFilterData(normalizeFilter(dataRaw.getFrequency(), dataRaw.getOnOffFilter(), dataRaw.isHighPass()));
        }

        data.setPan(normalizePan(dataRaw.getPan()));

        return data;
    }

    private int[] normalizePitch(double[] pitch) throws AppError {
        double[] scale = getRandomScale();

        pitch = quantizePitch(pitch);

        int[] output = new int[pitch.length];
        for (int i = 0; i < pitch.length; i++) {
            checkDouble(pitch[i], "pitch", i);

            double sum = 0.0;
            for (int offsetIndex = 0; offsetIndex < scale.length; offsetIndex++) {
                sum += scale[offsetIndex];
                if (pitch[i] <= sum) {
                    output[i] = 36 + offsetIndex;
                    break;
                }
            }
        }
        return output;
    }

    private double[] getRandomScale() {
        double[] scale = new double[] {
                2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f,
                2 / 76f,
                2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f,
                2 / 76f,
                2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f,
                2 / 76f,
                2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f, 2 / 76f, 1 / 76f,
                2 / 76f
        };

        int shift = new Random().nextInt(12);
        if (shift > 0) {
            double[] buffer = Arrays.copyOfRange(scale, 0, shift);
            for (int i = 0; i < scale.length; i++) {
                scale[i] = i < scale.length - shift ? scale[i + shift] : buffer[shift - (scale.length - i)];
            }
        }

        return scale;
    }

    /**
     * @param pitch data array which is presumed to be much longer than the amount
     *              of beats needed
     * @return data array that has the exact length where one data point can be
     *         sonified as one note
     *         compresses long data array to the required length by averaging a
     *         number of data points into one note.
     */
    private double[] quantizePitch(double[] pitch) {
        double[] notes = new double[numberBeats];
        int bufferLength = pitch.length / numberBeats;
        for (int i = 0, bufferStart = 0; i < notes.length; i++, bufferStart += bufferLength) {
            notes[i] = 0;
            for (int j = bufferStart; j < bufferStart + bufferLength; j++) {
                notes[i] += pitch[j] / bufferLength;
            }
        }

        return notes;
    }

    private double[] normalizeVolume(double[] relVolume, boolean[] absVolume) throws AppError {
        if (relVolume == null && absVolume == null) {
            return new double[] { 1.0 };
        } else if (relVolume == null) {
            double[] volume = new double[absVolume.length];
            for (int i = 0; i < absVolume.length; i++) {
                volume[i] = absVolume[i] ? 1.0 : 0.0;
            }
            return volume;
        } else {
            for (int i = 0; i < relVolume.length; i++) {
                checkDouble(relVolume[i], "relVolume", i);

                if (absVolume != null && absVolume.length > i) {
                    relVolume[i] = absVolume[i] ? relVolume[i] : 0.0;
                }
            }
            return relVolume;
        }
    }

    private int[] normalizeDelayEcho(double[] delayEcho) throws AppError {
        // TODO: test delay times
        if (delayEcho != null) {
            double[] delays = new double[] { 4 / 96f, 6 / 96f, 8 / 96f, 12 / 96f, 16 / 96f, 24 / 96f, 32 / 96f,
                    48 / 96f, 1f };
            int[] output = new int[delayEcho.length];

            for (int i = 0; i < delayEcho.length; i++) {
                checkDouble(delayEcho[i], "delayEcho", i);

                output[i] = (int) delays[(int) (delayEcho[i] * delays.length)]
                        * (Constants.SAMPLE_RATE * 60 / (Constants.TEMPO * 4));
            }
            return output;
        } else {
            return new int[] { (int) (12 / 96f * (Constants.SAMPLE_RATE * 60 / (Constants.TEMPO * 4))) };
        }
    }

    private double[] normalizeFeedbackEcho(double[] feedback, boolean[] onOff) throws AppError {
        // TODO: test values
        if (feedback != null) {
            for (int i = 0; i < feedback.length; i++) {
                checkDouble(feedback[i], "feedbackEcho", i);

                if (onOff != null && !onOff[i]) {
                    feedback[i] = 0.0;
                } else {
                    feedback[i] *= 0.9;
                }
            }
            return feedback;
        } else if (onOff != null) {
            feedback = new double[onOff.length];
            for (int i = 0; i < onOff.length; i++) {
                feedback[i] = onOff[i] ? 0.7 : 0.0;
            }
            return feedback;
        } else {
            return new double[] { 0.7 };
        }
    }

    private int[] normalizeDelayReverb(double[] delay) throws AppError {
        if (delay != null) {
            int[] output = new int[delay.length];
            for (int i = 0; i < delay.length; i++) {
                checkDouble(delay[i], "delayReverb", i);

                output[i] = (int) (delay[i] * 2205);
            }
            return output;
        } else {
            // TODO: test value
            return new int[] { 1600 };
        }
    }

    private double[] normalizeFeedbackReverb(double[] feedback, boolean[] onOff) throws AppError {
        // TODO: test values
        if (feedback != null) {
            for (int i = 0; i < feedback.length; i++) {
                checkDouble(feedback[i], "feedbackReverb", i);

                if (onOff != null && !onOff[i]) {
                    feedback[i] = 0.0;
                } else {
                    feedback[i] *= 0.8;
                }
            }
            return feedback;
        } else if (onOff != null) {
            feedback = new double[onOff.length];
            for (int i = 0; i < onOff.length; i++) {
                feedback[i] = onOff[i] ? 0.6 : 0.0;
            }
            return feedback;
        } else {
            return new double[] { 0.6 };
        }
    }

    private FilterData normalizeFilter(double[] cutoff, boolean[] onOff, boolean highPass) throws AppError {
        FilterData filter = new FilterData();

        if (cutoff != null) {
            for (int i = 0; i < cutoff.length; i++) {
                checkDouble(cutoff[i], "cutoff", i);

                if (onOff != null && !onOff[i]) {
                    cutoff[i] = highPass ? 50000 : 0;
                } else {
                    cutoff[i] = 40 + cutoff[i] * (20000 - 40);
                }
            }

            filter.setCutoff(cutoff);
        } else {
            cutoff = new double[onOff.length];
            for (int i = 0; i < onOff.length; i++) {
                cutoff[i] = highPass ? 1000 : 500;
            }
        }

        filter.setBandwidth(new double[] { 0.5 });
        filter.setHighPass(highPass);
        return filter;
    }

    private double[] normalizePan(double[] pan) throws AppError {
        if (pan != null) {
            for (int i = 0; i < pan.length; i++) {
                checkDouble(pan[i], "pan", i);

                pan[i] = (pan[i] * 2) - 1;
            }
            return pan;
        } else {
            return new double[] { 0.0 };
        }
    }

    private void checkDouble(double value, String collection, int index) throws AppError {
        if (value > 1 || value < 0) {
            throw new AppError("Mapped Data non-compliant: " + collection + "[" + index +
                    "] not in range(0,1) with value " + value);
        }
    }
}