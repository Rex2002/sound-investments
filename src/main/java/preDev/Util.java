package preDev;


import java.util.Arrays;
public class Util {

    public static short findMax(short[] array){
        short max = 0;
        for(short k : array){
            if(Math.abs(k) > max){
                max = (short) Math.abs(k);
            }
        }
        System.out.println("Found max value: " + max);
        return max;
    }

    public static int findMax(int[] array){
        return Arrays.stream(array).summaryStatistics().getMax();
    }
    public static int findMin(int[] array){
        return Arrays.stream(array).summaryStatistics().getMax();
    }

}