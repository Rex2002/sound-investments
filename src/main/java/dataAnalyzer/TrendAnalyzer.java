package dataAnalyzer;

import java.util.List;

import dataRepo.Price;

public interface TrendAnalyzer {
	double[] calculateMovingAverage(List<Price> prices);

	List<Double> calculateMinAndMax(List<Price> prices);

	boolean[] AverageIntersectsStock(double[] priceAverages, List<Price> prices);

	List<Boolean> GraphIntersectsMinMax(List<Price> prices, List<Double> minMax);
}
