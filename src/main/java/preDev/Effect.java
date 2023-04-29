package preDev;


public class Effect {

    public static byte[] onOffFilter(byte[] input, boolean[] onOff){
        double decayScalingFactor = 1; // controls how fast the sound decreases on change on -> off
        // TODO: implement attack-scaling on change off -> on
        for(int pos = 0; pos < input.length; pos++){
            if(!onOff[(int) (((double) pos/input.length) * onOff.length)] && pos >= 1){
                input[pos] = (byte) ((double) input[pos] * Math.pow(0.99, decayScalingFactor++/20));
            }
            if(onOff[(int) (((double) pos/input.length) * onOff.length)]){
                decayScalingFactor = 1;
                // reset scaling factor for next decay
            }
        }
        return input;
    }

    public static byte[] echo(byte[] input, double feedback, int delay){
        byte[] out = new byte[input.length];
        byte[] buffer = new byte[input.length];
        buffer[0] = input[0];
        int cursor = 0;
        for(int pos = 0; pos < input.length; pos++){
            byte v = input[pos];
            byte w = buffer[cursor];
            buffer[cursor++] = (byte) ((double) v + (double) w * feedback);
            //System.out.println("buffer value: " + buffer[cursor - 1]);
            if(cursor >= delay){
                cursor = 0;
            }
            out[pos] = w;
        }
        return out;
    }
}