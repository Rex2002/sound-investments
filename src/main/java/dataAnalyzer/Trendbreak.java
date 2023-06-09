package dataAnalyzer;

public class Trendbreak {
    public static boolean[] trendbreaks(boolean[] trends){ 
        boolean[] breakingPoint = new boolean[trends.length];
        for(int i=0; i<= trends.length; i++){
            if(trends[i]==true&&trends[i+1]==false){
                breakingPoint[i] = true;
            }
            else{
                breakingPoint[i] = false;
            }
        }
        return breakingPoint;
    }
}
