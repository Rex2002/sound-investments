package synth.fx;


import synth.Util;

import static synth.Test.SAMPLE_RATE;

public class Effect {

    static double maxGain = 1;

    public static short[] onOffFilter(short[] input, boolean[] onOff){
        double decayScalingFactor = 1; // controls how fast the sound decreases on change on -> off
        // TODO: implement attack-scaling on change off -> on
        for(int pos = 0; pos < input.length; pos++){
            if(!onOff[(int) (((double) pos/input.length) * onOff.length)] && pos >= 1){
                input[pos] = (short) ((double) input[pos] * Math.pow(0.99, decayScalingFactor++/20));
            }
            if(onOff[(int) (((double) pos/input.length) * onOff.length)]){
                decayScalingFactor = 1;
                // reset scaling factor for next decay
            }
        }
        return input;
    }

    public static short[] echoWithOverdrive(short[] input, double feedback, int delay){
        short[] out = new short[input.length];
        short[] bufferL = new short[input.length / 2];
        short[] bufferR = new short[input.length / 2];
        bufferL[0] = input[0];
        bufferR[0] = input[1];
        int cursor = 0;
        for(int pos = 0; pos < input.length/2; pos++){
            short inL = input[2 * pos];
            short inR = input[2 * pos + 1];
            short bL = bufferL[cursor];
            short bR = bufferR[cursor];
            bufferL[cursor] = (short) ((double) inL + (double) bL * feedback);
            bufferR[cursor] = (short) ((double) inR + (double) bR * feedback);
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            out[2 * pos] = bL;
            out[2 * pos + 1] = bR;
        }
        return out;
    }

    public static short[] echo(short[] input, double[] feedback, int[] delayArray){
        // TODO fix echo to deal with delayArray instead of fixed value (see Issue #31);
        int delay = delayArray[0];
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
            bufferL[cursor] = ( inL +  bL * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            bufferR[cursor] = ( inR +  bR * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            preOut[2 * pos] = bL;
            preOut[2 * pos + 1] = bR;
        }
        return Util.scale(preOut);
    }


    public static short[] IIR(short[] in, FilterData filterData){
        Coefficients c = new Coefficients();
        FILTER_TYPES ft = filterData.highPass ? FILTER_TYPES.HIGH : FILTER_TYPES.LOW;
        double[] kadov = filterData.getCutoff();
        // TODO create relation between order and bandwidth;
        double[] bandwidth = filterData.getOrder();
        calculateCoefficients(kadov[0], 0.5f, c, ft);
        int filterStart = 2 * Math.max(c.aCoefficients.length, c.bCoefficients.length);

        double[] preOut = new double[in.length];
        for(int i = 0; i < filterStart; i++){
            preOut[i] = in[i];
        }
        for(int i = filterStart; i < in.length; i++){
            calculateCoefficients(kadov[(int) (((double) (i-filterStart)/(in.length-filterStart)) * kadov.length)],0.5f, c, ft);
            double value = 0;
            for(int aPointer = 0; aPointer < c.aCoefficients.length; aPointer++){
                value += in[i - 2 * aPointer] * c.aCoefficients[aPointer];
            }
            for(int bPointer = 0; bPointer < c.bCoefficients.length; bPointer++){
                value += preOut[i - 2 * (bPointer + 1)] * c.bCoefficients[bPointer];
            }
            preOut[i] = value;
        }
        short maxIn = Util.findMax(in);
        double maxPreOut = Util.findMax(preOut);
        System.out.println("MaxIn " +  maxIn + ", maxPreOut: " +  maxPreOut);
        return Util.scale(preOut);
    }

    private static void calculateCoefficients(double kadov, double bandwidth, Coefficients c, FILTER_TYPES filterType){
        double t = Math.pow(10, Math.abs(maxGain) /20);
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