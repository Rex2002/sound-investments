package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;

public interface TrendAnalyzer {
    List<Double> calculateMovingAverage(List<Price> prices);
	List<Double> calculateMinAndMax(List<Price> prices);
	List<Boolean> AverageIntersectsStock(List<Price> prices);
}
