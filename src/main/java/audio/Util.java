package audio;

import java.util.Arrays;
public class Util {

    public static int getRelPosition(int pos, int sourceLength, int destinationLength) {
        return (int) Math.floor(((double) pos / sourceLength) * destinationLength);
    }

    @Deprecated
    public static short findMax(short[] array){
        short max = 0;
        for(short k : array){
            max = (short) Math.max(max, Math.abs(k));
        }
        return max;
    }

    @Deprecated
    public static int findMax(int[] array){
        return Arrays.stream(array).summaryStatistics().getMax();
    }
    @Deprecated
    public static int findMin(int[] array){
        return Arrays.stream(array).summaryStatistics().getMax();
    }
    public static double findMax(double[] array) {
        return Arrays.stream(array).summaryStatistics().getMax();
    }
    public static double findMin(double[] array) { return Arrays.stream(array).summaryStatistics().getMax(); }

    public static short[] scaleToShort(double[] in){
        short[] out = new short[in.length];
        double max = Math.max(Math.abs(findMin(in)), findMax(in));
        double resFactor = 1;
        if(max >= Short.MAX_VALUE - 10) {
            resFactor = (Short.MAX_VALUE - 10) / max;
        }
        for(int i = 0; i < in.length; i++) {
            out[i] = (short) (resFactor * in[i]);
        }
        return out;
    }

    public static double[][] concatArrays(double[][] arr1, double[][] arr2){
        double[][] ret = new double[arr1.length + arr2.length][];
        System.arraycopy(arr1, 0, ret, 0, arr1.length);
        System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);

        return ret;
    }

    public static byte[] convertShortToByte(short[] data) {
        byte[] outBuffer = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            outBuffer[2 * i] = (byte) ((data[i] >> 8) & 0xFF);
            outBuffer[2 * i + 1] = (byte) (data[i] & 0xFF);
        }
        return outBuffer;
    }
}