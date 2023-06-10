package audio.synth.fx;


import audio.Util;
import audio.synth.envelopes.ADSR;

import static audio.Constants.SAMPLE_RATE;

public class Effect {


    /**
     * the whole buffer-stuff is left as an exercise to the reader. <br/>
     * The interesting part is probably the ADSR feedbackEnv anyway. <br/>
     * The envelope solves the problem of crackings-sounds when the delay changes. <br/>
     * This is achieved by scaling down the feedback to zero, each time the delay value changes using the envelope. <br/>
     * Since the scaling of the feedback can not be done to fast, because then the cracking reappears, <br/>
     * this method is limited to delay arrays that have at most a length of 1/3 of the sound length in seconds. <br/>
     * (the length limiting of the delayArray is already done in the Harmonizer)
     * @param input the sound-array that is supposed to be echoed / reverbed
     * @param feedback the feedback values that are to be applied
     * @param delayArray the delay values that are to be applied
     * @return the input with respective reverb / echo effect
     */
    public static double[] echoWithFeedback(double[] input, double[] feedback, int[] delayArray){
        int delay = -1;
        double[] preOut = new double[input.length];
        double[] bufferL = new double[input.length / 2];
        double[] bufferR = new double[input.length / 2];
        bufferL[0] = input[0];
        bufferR[0] = input[1];
        double inL, inR, bL, bR, feedbackValue;
        int cursor = 0;
        int delayIdx = -1;
        int sectionOffset = 0, sectionLen;
        ADSR feedbackEnv = new ADSR(0.5, 0.02, 0.95, 0.46);
        for(int pos = 0; pos < input.length/2; pos++){
            if(delayIdx == -1 || (delayArray[delayIdx] != delayArray[Util.getRelPosition(pos, input.length/2, delayArray.length)])){
                delayIdx = Util.getRelPosition(pos, input.length/2, delayArray.length);
                delay = delayArray[delayIdx];
                sectionOffset = pos;
                sectionLen = 0;
                while(delayIdx + sectionLen < delayArray.length && delayArray[delayIdx] == delayArray[delayIdx + sectionLen]){
                    sectionLen++;
                }
                feedbackEnv.setSectionLen(input.length / (2 * delayArray.length) * sectionLen);
                bufferR = new double[delay];
                bufferL = new double[delay];
                cursor = 0;
            }
            if(delay != 0) {
                inL = input[2 * pos];
                inR = input[2 * pos + 1];
                bL = bufferL[cursor];
                bR = bufferR[cursor];
                feedbackValue = feedbackEnv.getAmplitudeFactor(pos - sectionOffset) * feedback[((int) (2 * (double) pos / input.length) * feedback.length)];
                bufferL[cursor] = inL + bL * feedbackValue;
                bufferR[cursor] = inR + bR * feedbackValue;
                preOut[2 * pos] = inL + bL * feedbackValue;
                ;
                preOut[2 * pos + 1] = inR + bR * feedbackValue;
                ;
                cursor += 1;
                if (cursor >= delay) {
                    cursor = 0;
                }
            }
            else{
                preOut[2 * pos] = input[2 * pos];
                preOut[2 * pos + 1] = input[2 * pos + 1];
            }
        }
        return preOut;
    }


    /**
     * @param in the data-array that is supposed to be anti-aliased
     * @return the input array after an anti-alias filter was applied
     */
    public static double[] antiAliasing(double[] in){
        FilterData antiAliasingFilterData = new FilterData();
        antiAliasingFilterData.setCutoff(new double[]{20000});
        antiAliasingFilterData.setBandwidth(new double[]{0.5});
        antiAliasingFilterData.setHighPass(false);
        return IIR(in, antiAliasingFilterData);
    }

    /**
     * using a recursive filter the input array is filtered. <br/>
     * The name is actually misleading, since an FIR could be realised with different coefficients, <br/>
     * but since the calculateCoefficients method only returns IIR-coefficients, it is probably somehow fine.
     * @param in the array that is supposed to be filtered
     * @param filterData object that contains information about the type of filtering, i.e. Cutoff-frequencies, Filtertype & bandwitdth
     * @return the filtered array
     */
    public static double[] IIR(double[] in, FilterData filterData){
        Coefficients c = new Coefficients();
        FilterTypesEnum ft = filterData.highPass ? FilterTypesEnum.HIGH : FilterTypesEnum.LOW;
        double[] kadov = filterData.getCutoff();
        double[] bandwidth = filterData.getBandwidth();
        calculateCoefficients(kadov[0], bandwidth[0], c, ft);
        int filterStart = 2 * Math.max(c.aCoefficients.length, c.bCoefficients.length);

        double[] out = new double[in.length];
        if (filterStart >= 0) System.arraycopy(in, 0, out, 0, filterStart);
        for(int i = filterStart; i < in.length; i++){
            calculateCoefficients(
                    kadov[Util.getRelPosition((i-filterStart), in.length-filterStart, kadov.length)],
                    bandwidth[Util.getRelPosition((i-filterStart), in.length-filterStart, bandwidth.length)],
                    c, ft
            );
            double value = 0;
            for(int aPointer = 0; aPointer < c.aCoefficients.length; aPointer++){
                value += in[i - 2 * aPointer] * c.aCoefficients[aPointer];
            }
            for(int bPointer = 0; bPointer < c.bCoefficients.length; bPointer++){
                value += out[i - 2 * (bPointer + 1)] * c.bCoefficients[bPointer];
            }
            out[i] = value;
        }
        return out;
    }

    /**
     *  based on the inputs the function calculates IIR (and theoretically FIR) filter coefficients
     * @param kadov cutoff frequency for which the coefficients are to be calculated
     * @param bandwidth bandwidth that the filter is supposed to have
     * @param c the object into which the result is supposed to be written
     * @param filterType the type of the filter that the coefficients shall realise
     */
    @SuppressWarnings("DuplicateExpressions")
    private static void calculateCoefficients(double kadov, double bandwidth, Coefficients c, FilterTypesEnum filterType){
        double x = Math.tan(Math.PI * kadov/SAMPLE_RATE);
        double norm, a0, a1, a2, b1, b2;

        switch (filterType){
            case FOUR_STAGE_LOW -> {
                x = Math.pow(Math.E, -14.4445 * kadov/SAMPLE_RATE);
                c.aCoefficients = new double[]{
                        Math.pow((1-x), 4)
                };
                c.bCoefficients = new double[]{
                        4*x,
                        -6 * x * x,
                        4 * Math.pow(x,3),
                        - Math.pow(x,4)
                };

            }
            case LOW -> {
                norm = 1 / (1 + x / bandwidth + x * x);
                a0 = x * x * norm;
                a1 = 2 * a0;
                a2 = a0;
                b1 = 2 * (x * x - 1) * norm;
                b2 = (1 - x / bandwidth + x * x) * norm;
                c.aCoefficients = new double[]{a0,a1,a2};
                c.bCoefficients = new double[]{-b1, -b2};
            }
            case HIGH -> {
                norm = 1 / (1 + x / bandwidth + x * x);
                a0 = 1 * norm;
                a1 = -2 * a0;
                a2 = a0;
                b1 = 2 * (x * x - 1) * norm;
                b2 = (1 - x / bandwidth + x * x) * norm;
                c.aCoefficients = new double[]{a0,a1,a2};
                c.bCoefficients = new double[]{-b1, -b2};
            }
        }

    }

}