package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class Blur {
    public static final int BLUR_LENGTH = 5;

    public static double[] averageBlur(List<Price> prices) {
        int size = prices.size();
        double[] blurredValues = new double[size];

        for (int i = 0; i < size; i++) {
            int start = Math.max(0, i - BLUR_LENGTH);
            int end = Math.min(size - 1, i + BLUR_LENGTH);

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
