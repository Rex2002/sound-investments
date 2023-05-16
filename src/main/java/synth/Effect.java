package synth;


import static synth.Test.SAMPLE_RATE;

public class Effect {

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

    public static short[] simplestLowPass(short[] in){
        //TODO untested, probably stupid!!
        short[] out = new short[in.length];
        for(int i = 2; i< in.length; i += 2){
            out[i] += (short) (in[i] - in[i - 2]);
            out[i + 1] += (short) (in[i + 1] - in[i - 1]);
        }
        return out;
    }

    public static short[] fourStageLowPass(short[] in, double kadov){
        double x = Math.pow(Math.E, -14.4445 * kadov/SAMPLE_RATE);
        double[] a_coefficients = new double[]{Math.pow((1-x), 4)};
        double[] b_coefficients = new double[]{4*x, -6 * x * x, 4 * Math.pow(x,3), - Math.pow(x,4)};

        return IIR(in, a_coefficients, b_coefficients);
    }

    private static short[] IIR(short[] in, double[] a_coefficients, double[] b_coefficients){
        int filterStart = Math.max(a_coefficients.length, b_coefficients.length);

        double[] preOut = new double[in.length];
        for(int i = 0; i < filterStart; i++){
            preOut[i] = in[i];
        }
        for(int i = filterStart; i < in.length; i++){
            double value = 0;
            for(int aPointer = 0; aPointer < a_coefficients.length; aPointer++){
                value += in[i - aPointer] * a_coefficients[aPointer];
            }
            for(int bPointer = 0; bPointer < b_coefficients.length; bPointer++){
                value += preOut[i -(bPointer + 1)] * b_coefficients[bPointer];
            }
            preOut[i] = value;
        }
        short maxIn = Util.findMax(in);
        double maxPreOut = Util.findMax(preOut);
        System.out.println("MaxIn " +  maxIn + ", maxPreOut" +  maxPreOut);
        return Util.scale(preOut);
    }
}