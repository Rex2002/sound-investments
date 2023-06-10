package audio.harmonizer;

import app.AppError;
import app.mapping.GlobalFxDataRaw;
import app.mapping.InstrumentDataRaw;
import audio.Constants;
import audio.Util;
import audio.synth.GlobalFxData;
import audio.synth.InstrumentData;
import audio.synth.fx.FilterData;

import java.util.Arrays;
import java.util.Random;


/**
 * The main purpose of this class is to translate Market/Stock-domain information to the music-domain. <br/>
 * This is needed in two ways: <br/>
 *  Raw InstrumentData needs to be translated  toInstrumentData that is understood by the SynthLine-class <br/>
 *  Raw GlobalFxData needs to be translated to GlobalFxData that is understood by the sonify-method <br/>
 *
 *  Since the operations performed by the latter are a subset of the operations performed by the first, the latter is included into this class as well. <br/>
 *  Please note therefor that calling harmonizeGlobalData only yields results, when the constructor was called with an object of the GlobalFxDataRaw-type and vice versa. <br/>
 */
public class Harmonizer {
    private InstrumentDataRaw dataRaw;
    private GlobalFxDataRaw gDataRaw;
    private final int numberBeats;

    /**
     * Constructor that should be used, if Instruments are to be harmonized.
     * @param dataRaw raw instrumentData
     * @param numberBeats target length of the Sonification
     */
    public Harmonizer(InstrumentDataRaw dataRaw, int numberBeats) {
        this.dataRaw = dataRaw;
        this.numberBeats = numberBeats;
    }

    /**
     * Constructor that should be used, if GlobalFx are to be harmonized.
     * @param dataRaw raw GlobalFxData
     * @param numberBeats target length of the Sonification
     */
    public Harmonizer(GlobalFxDataRaw dataRaw, int numberBeats){
        this.numberBeats = numberBeats;
        this.gDataRaw = dataRaw;
    }

    /**
     * Method orchestrating the harmonizing of the raw globalFxData. <br/>
     * The GlobalFx-harmonization consists of filter-normalization and feverb-normalization
     * @return a GlobalData-Object that is understood by the Sonify-method and can be used to apply global effects
     * @throws AppError if an error occurs the user is informed
     */
    public GlobalFxData harmonizeGlobalData() throws AppError{
        if(gDataRaw == null){
            throw new AppError("Interner Fehler bei der Global-FX-Harmonisierung");
        }
        GlobalFxData data = new GlobalFxData();
        if (!(gDataRaw.getCutOffFrequency() == null && gDataRaw.getOnOffFilter() == null)) {
            data.setFilterData(normalizeFilter(gDataRaw.getCutOffFrequency(), gDataRaw.getOnOffFilter(), gDataRaw.isHighPass()));
        }
        if(gDataRaw.getDelayReverb() != null && gDataRaw.getFeedbackReverb() != null){
            data.setDelayReverb(normalizeDelayReverb(gDataRaw.getDelayReverb()));

            data.setFeedbackReverb(normalizeFeedbackReverb(gDataRaw.getFeedbackReverb(), gDataRaw.getDelayReverb(), gDataRaw.getOnOffReverb()));
        }

        return data;
    }

    /**
     * Method orchestrating the harmonizing of the raw InstrumentData. <br/>
     * The Harmonization consists of pitch-, volume-, echo-, reverb-, filter- and pan-normalization
     * @return an InstrumentData-Object that is understood by the SynthLine and can be used to synthesize the corresponding sound
     * @throws AppError if an error occurs the user is informed
     */
    public InstrumentData harmonizeInstrumentData() throws AppError {
        if(dataRaw == null){
            throw new AppError("Interner Fehler bei der Instrument-Harmonisierung");
        }
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
            data.setDelayReverb(normalizeDelayReverb(dataRaw.getDelayReverb()));

            data.setFeedbackReverb(normalizeFeedbackReverb(dataRaw.getFeedbackReverb(), dataRaw.getDelayReverb(), dataRaw.getOnOffReverb()));
        }

        if (!(dataRaw.getFrequency() == null && dataRaw.getOnOffFilter() == null)) {
            data.setFilterData(normalizeFilter(dataRaw.getFrequency(), dataRaw.getOnOffFilter(), dataRaw.isHighPass()));
        }


        data.setPan(normalizePan(dataRaw.getPan()));

        return data;
    }

    /**
     * Pitch normalization is done in three steps:<br/>
     * 1. selecting a music-key-pattern<br/>
     * 2. quantizing the available data (i.e. making sure that exactly one pitch value is present for each beat)<br/>
     * 3. creating midi-note-values based on the quantized pitch values and the scale that was selected in step one.<br/>
     * For an in-depth understanding on how the scale is incorporated into the result, it is probably best to refer to the architecture document.
     * @return an int-array that contains one midi-note between 36 and 83 for each beat.
     * @throws AppError if an invalid values is detected in the pitch, an AppError is thrown to inform the user.
     */
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
                if (pitch[i] <= sum / (scale.length * NUMBER_OCTAVES)) {
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
     * compresses long data array to the required length by averaging a number of data points into one note. <br/>
     * @return data array that has the exact length where one data point can be
     *         sonified as one quarter note
     */
    private double[] quantizePitch() {
        double[] pitch = dataRaw.getPitch();
        double[] notes = new double[numberBeats];
        int bufferLength = pitch.length / numberBeats;
        if (bufferLength > 0) {
            for (int i = 0, bufferStart = 0; i < notes.length; i++, bufferStart += bufferLength) {
                notes[i] = 0;
                if (pitch[bufferStart] == -1 || pitch[bufferStart + bufferLength - 1] == -1) {
                    notes[i] = -1;
                    continue;
                }
                for (int j = bufferStart; j < bufferStart + bufferLength; j++) {
                    notes[i] += pitch[j] / bufferLength;
                }
            }
        } else {
            for (int i = 0; i < notes.length; i++) {
                notes[i] = pitch[Util.getRelPosition(i, notes.length, pitch.length)];
            }
        }

        return notes;
    }

    /**
     * the volume creates an array that contains the volumes based on the provided absolute and relative volumes. <br/>
     * The combining of the absolute and relative volumes is done in such a way that the relative volume is applied when the absolute volume is True. <br/>
     * and that muting is done, when the absolute volume is False, i.e. absolute volume overrides relative volume to a certain extent. <br/>
     * If only absolute volume is provided, the resulting volume is the MAX_VOLUME for every datapoint of the absolute volume that reads True and MUTE_VOLUME for every False-datapoint <br/>
     * If neither is provided, the volume is set to MAX_VOLUME.
     * Furthermore, in each of the cases, the volume is set to MUTE_VOLUME if no pitch is available for this datapoint (e.g. because one stock was not traded at that time) and to MAX_VOLUME otherwise
     * @return double array that contains the normalized volume
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
    private double[] normalizeVolume() throws AppError {
        double MAX_VOLUME = 1.0;
        double MUTE_VOLUME = 0.0;
        double[] relVolume = dataRaw.getRelVolume();
        boolean[] absVolume = dataRaw.getAbsVolume();
        double[] pitch = dataRaw.getPitch();
        if (relVolume == null && absVolume == null) {
            double[] volume = new double[pitch.length];
            for (int i = 0; i < volume.length; i++) {
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
                    relVolume[i] = absVolume[i] && pitch[i] != -1 && relVolume[i] != -1 ? relVolume[i] : MUTE_VOLUME;
                }
            }
            return relVolume;
        }
    }

    /**
     * Since echo only sounds good when it is somehow rhythmically related to the tempo/beat, the provided delay-values are quantized to represent meaningful numbers of samples, <br/>
     * e.g. the length of a quarter, a sixteenth, etc. <br/>
     * The method is designed to yield more short delay-times (for an evenly distributed input), because long-delays would not necessarily reflect the actual market-state at the time they can be heard.<br/>
     * @return an int array that contains the number of samples for the echo-delay
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
    private int[] normalizeDelayEcho() throws AppError {
        int SAMPLES_PER_BAR = Constants.SAMPLE_RATE * 60 / (Constants.TEMPO / 4);
        int DEFAULT_DELAY = (int) (1 / 8f * SAMPLES_PER_BAR);
        double[] delayEcho = dataRaw.getDelayEcho();
        double[] delays = new double[] { 1 / 24f, 1 / 16f, 1 / 12f, 1 / 8f, 1 / 6f, 1 / 4f, 1 / 3f, 1 / 2f, 1 };

        return normalizeDelayGeneric(false, delayEcho, DEFAULT_DELAY, delays);
    }

    /**
     * The feedback is basically just scaled to a maximum value of 0.9, (because values closer to 1.0 tend to create screeching sounds)
     * Additionally the method sets the feedback to 0 if at the time of the feedback either the echoOnOff-array is false
     * or the delay-array has no valid values (i.e. is -1, meaning no stock-market data available for this moment in time).
     * If no feedback is given, but onOff exists, the feedback is set to the DEFAULT_FEEDBACK_ECHO value at every True-datapoint.
     * @return the normalized feedback between 0 and MAX_FEEDBACK_ECHO
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
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
        } else if (delayEcho != null) {
            feedbackEcho = new double[delayEcho.length];
            for (int i = 0; i < delayEcho.length; i++) {
                feedbackEcho[i] = delayEcho[i] == -1 ? MUTE_ECHO : DEFAULT_FEEDBACK_ECHO;
            }
        } else {
            feedbackEcho = new double[] { DEFAULT_FEEDBACK_ECHO };
        }
        return feedbackEcho;
    }

    /**
     * Since reverb is basically echo with delay times of less than 50ms,
     * this method primarily has to normalize the inputs to sample-numbers that represent reverberation-times between 0 and 50ms;
     * @return an int array that contains the number of samples for the reverb-delay
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
    private int[] normalizeDelayReverb(double[] delayReverb) throws AppError {
        int MAX_DELAY_REVERB = Constants.SAMPLE_RATE / 20; // number of samples corresponding to 50ms
        int DEFAULT_DELAY_REVERB = Constants.SAMPLE_RATE / 25; // number of samples corresponding to 40ms
        //double[] delayReverb = dataRaw.getDelayReverb();

        return normalizeDelayGeneric(true, delayReverb, DEFAULT_DELAY_REVERB, new double[]{ MAX_DELAY_REVERB });

    }

    /**
     * Some generic-delay-normalization-maigc
     * @param reverb
     * @param delayInput
     * @param defaultDelayTime
     * @param delayTimes
     * @return
     * @throws AppError
     */
    private int[] normalizeDelayGeneric(boolean reverb, double[] delayInput, int defaultDelayTime, double[] delayTimes) throws AppError {
        int SAMPLES_PER_BAR = Constants.SAMPLE_RATE * 60 / (Constants.TEMPO / 4);
        int maxNoDelayValues = (int) (1.0/3 * (numberBeats / (Constants.TEMPO / 60f)));

        if (delayInput != null) {
            if (delayInput.length <= maxNoDelayValues) {
                int[] output = new int[delayInput.length];
                for (int i = 0; i < output.length; i++) {
                    double delayValue = delayInput[i];
                    checkDouble(delayValue, "delayInput", i);
                    if (reverb) {
                        output[i] = delayValue == -1 ? defaultDelayTime : (int) (delayValue * delayTimes[0]);
                    } else {
                        output[i] = delayValue == -1 ? defaultDelayTime : (int) (delayTimes[(int) (delayValue * (delayTimes.length - 1))] * SAMPLES_PER_BAR);
                    }
                }
                return output;
            }
            else {
                int bufferLength = delayInput.length / maxNoDelayValues;
                int[] output = new int[maxNoDelayValues];
                for (int i = 0, bufferStart = 0; i < maxNoDelayValues; i++, bufferStart += bufferLength) {
                    output[i] = 0;
                    if (delayInput[bufferStart] == -1 || delayInput[bufferStart + bufferLength - 1] == -1) {
                        output[i] = defaultDelayTime;
                        continue;
                    }
                    double tmpForPreciseAddition = 0;
                    for (int j = bufferStart; j < bufferStart + bufferLength; j++) {
                        checkDouble(delayInput[j], reverb ? "delayReverb" : "delayEcho", j);
                        tmpForPreciseAddition += delayInput[j] / bufferLength;
                    }
                    if (reverb) {
                        output[i] = (int)(tmpForPreciseAddition * delayTimes[0]);
                    } else {
                        output[i] = (int) (delayTimes[(int) (tmpForPreciseAddition * (delayTimes.length - 1))] * SAMPLES_PER_BAR);
                    }

                }
                return output;
            }
        } else {
            return new int[] { defaultDelayTime };
        }
    }

    /**
     * basically the same as normalizeFeedbackEcho, just with different default values.
     * This method receives the input data as parameters, because it is used by both the InstrumentHarmonization and the GlobalFx-Harmonization,
     * hence only at runtime it is known from where the data that is supposed to processed would have to be taken.
     * @param feedbackReverb feedback input array
     * @param delayReverb delay input array
     * @param onOffReverb on-off input array
     * @return an array containing the normalized feedback
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
    private double[] normalizeFeedbackReverb(double[] feedbackReverb, double[] delayReverb, boolean[] onOffReverb) throws AppError {
        double MAX_FEEDBACK_REVERB = 0.8;
        double DEFAULT_FEEDBACK_REVERB = 0.6;
        double MUTE_REVERB = 0;
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
            for (int i = 0; i < delayReverb.length; i++) {
                feedbackReverb[i] = delayReverb[i] == -1 ? MUTE_REVERB : DEFAULT_FEEDBACK_REVERB;
            }
        } else {
            feedbackReverb = new double[] { DEFAULT_FEEDBACK_REVERB };
        }
        return feedbackReverb;
    }

    /**
     * combines the cutoff-frequency array and the on-off-frequency array to one cutoff-frequency array for the corresponding filter-type (described by the highPass-Parameter).
     * @param cutoff
     * @param onOff
     * @param highPass
     * @return a FilterData object that contains all the data needed by the Effect.IIR to properly create the desired filter-effect
     * @throws AppError
     */
    private FilterData normalizeFilter(double[] cutoff, boolean[] onOff, boolean highPass) throws AppError {
        double BANDWIDTH = 0.5;
        int MIN_FREQ = 40;
        int MAX_FREQ = 20000;
        int HIGH_PASS_OFF = 1;
        int HIGH_PASS_DEFAULT_FREQ = 1000;
        int LOW_PASS_OFF = 22050;
        int LOW_PASS_DEFAULT_FREQ = 500;
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

    /**
     * Pan normalization normalizes the panning by decreasing one channel if the input is below 0.5 and decreasing the other one for values above 0.5.
     * @param pan
     * @return the data-array containing the normalized panning values
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
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

    /**
     * method to check if a given value is valid (i.e. is -1.0 or between 0.0. and 1.0).
     * Every other value results in an error being shown to the user.
     * @param value that is to be checked
     * @param collection type of source-array of the value
     * @param index position of the value in the source array
     * @throws AppError if invalid datapoints are discovered, the user is informed of that
     */
    private void checkDouble(double value, String collection, int index) throws AppError {
        // -1 is needed, because that is the internal "off"/switch
        if ((value > 1 || value < 0) && value != -1) {
            throw new AppError("Ungültige Daten: " + collection + "[" + index +
                    "]: " +value + " liegt außerhalb des zugelassenen Bereichs von [0,1]. ");
        }
    }
}