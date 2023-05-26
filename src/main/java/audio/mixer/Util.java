package audio.mixer;

import java.util.LinkedList;

public class Util {
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

    public static short findMax(LinkedList<short[]> arrayList){
        short max = 0;
        for(short[] array : arrayList){
            for(short k : array){
                if(Math.abs(k) > max){
                    max = (short) Math.abs(k);
                }
            }
        }
        //System.out.println("Found max value: " + max);
        return max;
    }
}
