package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class Blurr {
    public static double[] gaussianBlur(List<Price> prices) {
        int size = prices.size();
        double[] blurredValues = new double[size];
        
        for (int i = 0; i < size; i++) {
            int start = Math.max(0, i - 5);
            int end = Math.min(size - 1, i + 5);
            
            double sum = 0.0;
            int count = 0;
            
            for (int j = start; j <= end; j++) {
                Price price = prices.get(j);
                sum += price.low + price.high + price.close + price.open;
                count += 4;
            }
            
            blurredValues[i] = sum / count;
        }
        
        return blurredValues;
    }
}
