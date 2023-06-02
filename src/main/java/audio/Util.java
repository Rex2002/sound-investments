package audio;


import audio.synth.Complex;

import java.util.Arrays;
public class Util {

    public static Complex[] fft(short[] vTd){
        Complex[] complexValues = new Complex[(int) Math.pow(2, (int) (Math.log(vTd.length)/Math.log(2)))];
        for(int i = 0 ; i< complexValues.length; i++){
            complexValues[i] = new Complex(vTd[i], 0);
        }
        transform(complexValues);
        return complexValues;
    }

    public static void transform (Complex[] c){
        if (c.length <= 1){
            return;
        }
        Complex[] even = new Complex[c.length/2];
        Complex[] odd  = new Complex[c.length/2];

        for(int i = 0; i < c.length; i++){
            if( i % 2 == 0){
                even[i/2] = c[i];
            }
            else{
                odd[(i-1)/2] = c[i];
            }
        }
        transform(even);
        transform(odd);

        for(int i = 0; i < c.length / 2; i ++){
            double kth = -2 * i * Math.PI / c.length;
            Complex t = new Complex(Math.cos(kth), Math.sin(kth)).multiply(odd[i]);
            c[i] = even[i].add(t);
            c[i + c.length/ 2] = even[i].subtract(t);
        }
    }

    public static int getRelPosition(int pos, int sourceLength, int destinationLength){
        return (int) (((double) pos / sourceLength) * destinationLength);
    }

    public static short findMax(short[] array){
        short max = 0;
        for(short k : array){
            if(Math.abs(k) > max){
                max = (short) Math.abs(k);
            }
        }
        //System.out.println("Found max value: " + max);
        return max;
    }

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