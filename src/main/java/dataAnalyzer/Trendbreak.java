package dataAnalyzer;

public class Trendbreak {
    public static boolean[] trendbreaks(boolean[] trends){
        boolean[] breakingPoints = new boolean[trends.length];
        for(int i=0; i<= trends.length; i++){
            if(trends[i]==true&&trends[i+1]==false){
                breakingPoints[i] = true;
            }
            else{
                breakingPoints[i] = false;
            }
        }
        return breakingPoints;
    }
}
