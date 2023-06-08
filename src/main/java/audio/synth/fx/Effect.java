package audio.synth.fx;


import audio.Util;

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

    public static double[] copyOfOneChannel(double[] in, int len, int start){
        double[] out = new double[len];
        for(int i = 0; i < len ; i ++){
            out[i] = in[start + 2 * i];
        }
        return out;
    }

    public static double[] echoPart(double[] input, double[] feedback, int delay, int start, int length, int overlap){
        if(delay == 0){
            return input;
        }
        double[] bufferL = new double[delay];//copyOfOneChannel(input, delay, start);
        double[] bufferR = new double[delay];//copyOfOneChannel(input, delay, start + 1);
        double inL, inR, bL, bR;
        int cursor = 0;
        for(int pos = start; pos < start + length + overlap; pos += 2){
            inL = input[pos];
            inR = input[pos + 1];
            bL = bufferL[cursor];
            bR = bufferR[cursor];
            bufferL[cursor] = inL +  bL * (pos > start+length ? 0.1 : feedback[Util.getRelPosition(pos, input.length, feedback.length)]);
            bufferR[cursor] = inR + bR * (pos > start+length ? 0.1 : feedback[Util.getRelPosition(pos, input.length, feedback.length)]);
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            input[pos] = bL;
            input[pos + 1] = bR;
        }
        return input;
    }

    public static double[] reverb(double[] input, double[] feedback, int[] delayArray){
        int overlap = 44100;
        int sectionSize;
        int sectionLen = input.length / delayArray.length;
        int delay = -1;
        int delayPointer = 0;
        while(delayPointer < delayArray.length){
            delay = delayArray[delayPointer];
            sectionSize = 0;
            do{
                sectionSize++;
            } while( delayPointer + sectionSize < delayArray.length && delayArray[delayPointer] == delayArray[delayPointer + sectionSize]);
            input = echoPart(input, feedback, delay, delayPointer * sectionLen,
                    delayPointer * sectionLen + sectionLen * sectionSize < input.length ? sectionLen * sectionSize : input.length - delayPointer * sectionLen > 0 ? input.length - delayPointer * sectionLen : 0,
                    delayPointer * sectionLen + sectionLen * sectionSize + overlap < input.length ? overlap : 0);
            delayPointer += sectionSize;
        }
        return input;
    }

    public static double[] anotherGoAtReverb(double[] input, double[] feedback, int[] delayArray){
        int sectionLen = input.length / delayArray.length;
        int delayPointer = 0;
        int delay, sectionSize;
        while(delayPointer < delayArray.length){
            delay = delayArray[delayPointer];
            sectionSize = 0;
            do {
                sectionSize++;
            }while(delayPointer + sectionSize < delayArray.length && delayArray[delayPointer] == delayArray[delayPointer + sectionSize]);
            input = echo(input, feedback, delay, delayPointer * sectionLen, sectionSize * sectionLen);
            delayPointer += sectionSize;
        }
        System.out.println("finished applying reverb");
        return input;
    }

    public static double[] echo(double[] input, double[] feedback, int delay, int start, int length){
        if(delay <= 10){
            return input;
        }
        double[] preOut = new double[input.length];
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
            bufferL[cursor] = ( inL +  bL * ((pos > start && pos < start + length) ? feedback[((int) (2 * (double) pos/input.length) * feedback.length)] : 0.1));
            bufferR[cursor] = ( inR +  bR * ((pos > start && pos < start + length) ? feedback[((int) (2 * (double) pos/input.length) * feedback.length)] : 0.1));
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            preOut[2 * pos] = bL;
            preOut[2 * pos + 1] = bR;
        }
        System.out.println("Max after another go: " + Util.findAverage(preOut)/Util.findMax(preOut));
//        System.out.println("Average: " + Util.findAverage(preOut));
        return preOut;
    }


    public static double[] echo(double[] input, double[] feedback, int[] delayArray){
        int delay;
        double[] preOut = new double[input.length];
        double[] bufferL = new double[input.length / 2];
        double[] bufferR = new double[input.length / 2];
        bufferL[0] = input[0];
        bufferR[0] = input[1];
        int cursor = 0;
        for(int pos = 0; pos < input.length/2; pos++){
            delay = delayArray[Util.getRelPosition(pos, input.length, delayArray.length)];
            double inL = input[2 * pos];
            double inR = input[2 * pos + 1];
            double bL = bufferL[cursor];
            double bR = bufferR[cursor];
            bufferL[cursor] = ( inL +  bL * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            bufferR[cursor] = ( inR +  bR * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            preOut[2 * pos] = bL;
            preOut[2 * pos + 1] = bR;
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