package synth;


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
        short[] outShort = new short[input.length];
        int[] out = new int[input.length];
        int[] bufferL = new int[input.length / 2];
        int[] bufferR = new int[input.length / 2];
        bufferL[0] = input[0];
        bufferR[0] = input[1];
        int cursor = 0;
        for(int pos = 0; pos < input.length/2; pos++){
            int inL = input[2 * pos];
            int inR = input[2 * pos + 1];
            int bL = bufferL[cursor];
            int bR = bufferR[cursor];
            bufferL[cursor] = (int) ((double) inL + (double) bL * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            bufferR[cursor] = (int) ((double) inR + (double) bR * feedback[((int) (2 * (double) pos/input.length) * feedback.length)]);
            cursor += 1;
            if(cursor >= delay){
                cursor = 0;
            }
            out[2 * pos] = bL;
            out[2 * pos + 1] = bR;
        }
        int max = Math.max(Util.findMax(out), Math.abs(Util.findMin(out)));
        double resizingFactor = 1;
        if(max >= Short.MAX_VALUE){
            // this is slightly less than short.MAX_VALUE to avoid punctual overdrive
            resizingFactor = 32755f / max;
        }
        for (int i = 0; i < out.length; i++) {
            outShort[i] = (short) (out[i] * resizingFactor);
        }
        return outShort;
    }

    public static short[] simplestLowPass(short[] in){
        //TODO untested!!
        short[] out = new short[in.length];
        for(int i = 2; i< in.length; i += 2){
            out[i] += (short) (in[i] - in[i - 2]);
            out[i + 1] += (short) (in[i + 1] - in[i - 1]);
        }
        return out;
    }
}