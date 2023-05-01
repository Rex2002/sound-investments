package preDev;


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

    public static short[] echo(short[] input, double feedback, int delay){
        short[] out = new short[input.length];
        short[] buffer = new short[input.length];
        buffer[0] = input[0];
        int cursor = 0;
        for(int pos = 0; pos < input.length; pos++){
            short v = input[pos];
            short w = buffer[cursor];
            buffer[cursor++] = (short) ((double) v + (double) w * feedback);
            //System.out.println("buffer value: " + buffer[cursor - 1]);
            if(cursor >= delay){
                cursor = 0;
            }
            out[pos] = w;
        }
        return out;
    }
}