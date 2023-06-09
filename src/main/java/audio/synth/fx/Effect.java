package audio.synth.fx;


import audio.Util;
import audio.synth.envelopes.ADSR;

import static audio.Constants.SAMPLE_RATE;

public class Effect {

    @Deprecated
    public static double[] onOffFilter(double[] input, boolean[] onOff){
        double decayScalingFactor = 1; // controls how fast the sound decreases on change on -> off
        // TODO: implement attack-scaling on change off -> on
        for(int pos = 0; pos < input.length; pos++){

            if(!onOff[Util.getRelPosition(pos, input.length, onOff.length)] && pos >= 1){
                input[pos] = input[pos] * Math.pow(0.99, decayScalingFactor++/20);
            }
            if(onOff[Util.getRelPosition(pos, input.length, onOff.length)]){
                decayScalingFactor = 1;
                // reset scaling factor for next decay
            }
        }
        return input;
    }

    @Deprecated
    public static double[] echoWithOverdrive(double[] input, double feedback, int delay){
        double[] out = new double[input.length];
        double[] bufferL = new double[input.length / 2];
        double[] bufferR = new double[input.length / 2];
        bufferL[0] = input[0];
        bufferR[0] = input[1];
        int cursor = 0;
        for(int pos = 0; pos < input.length/2; pos++){
            double inL = input[2 * pos];
            double inR = input[2 * pos + 1];
            double bL = bufferL[cursor];
            double bR = bufferR[cursor];
            bufferL[cursor] = inL + bL * feedback;
            bufferR[cursor] = inR + bR * feedback;
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            out[2 * pos] = bL;
            out[2 * pos + 1] = bR;
        }
        return out;
    }




    public static double[] echoWithFeeeeedback(double[] input, double[] feedback, int[] delayArray){
        System.out.println("delay Array length: " + delayArray.length);
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


    public static double[] antiAliasing(double[] in){
        FilterData antiAliasingFilterData = new FilterData();
        antiAliasingFilterData.setCutoff(new double[]{20000});
        antiAliasingFilterData.setBandwidth(new double[]{0.5});
        antiAliasingFilterData.setHighPass(false);
        return IIR(in, antiAliasingFilterData);
    }


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