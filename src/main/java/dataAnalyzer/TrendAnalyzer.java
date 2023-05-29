package dataAnalyzer;

import java.util.List;

public interface TrendAnalyzer {
    List<PriceData> calculateMovingAverage(List<Price> prices);
	List<Double> calculateMinAndMax(List<Price> prices);
	List<Boolean> AverageIntersectsStock(List<PriceData> priceAverages, List<Price> prices);
	List<Boolean> GraphIntersectsMinMax(List<Price> prices, List<Double> minMax);
}
