package dhbw.si.dataAnalyzer;

import dhbw.si.dataRepo.Price;
import java.util.List;

/**
 * @author J. Kautz
 */

public class GeneralTrends {
	/**
	 * 	Implements calculation of Moving Average
	 * 	returns moving average for every point in time as double[]
	 */
	public static double[] calculateMovingAverage(List<Price> priceList) {
		double[] priceAverages = new double[priceList.size()];
		double[] previousPrices = new double[200];

		for (int i = 0; i < priceList.size(); i++) {
			Price price = priceList.get(i);
			previousPrices[i % previousPrices.length] = (price.getLow() + price.getHigh() + price.getOpen() + price.getClose()) / 4;
			priceAverages[i] = calculateAverage(previousPrices, i >= previousPrices.length ? previousPrices.length : i+1);
		}

		return priceAverages;
	}

	private static double calculateAverage(double[] prices, int len) {
		double sum = 0.0;
		for (int i = 0; i < len; i++) {
			sum += prices[i];
		}
		return sum / len;
	}

	public static boolean[] AverageIntersectsStock(double[] priceAverages, List<Price> prices) {
		boolean[] intersections = new boolean[priceAverages.length];

		// Implementiert eine Funktion welche Testet, ob der Moving Average für einen
		// Zeitpunkt gleich dem Stockpreis ist
		// Gibt die Ergebnise als Liste von Booleans zurück

		for (int i = 0; i < intersections.length; i++) {
			intersections[i] = prices.get(i).getLow() <= priceAverages[i]
					&& priceAverages[i] <= prices.get(i).getHigh();
		}

		return intersections;
	}

	public static double[] calculateSlope(List<Price> priceList) {
		double[] slope = new double[priceList.size()];
		for (int i = 0; i < priceList.size(); i++) {
			double graphClose = priceList.get(i).getClose();
			double graphOpen = priceList.get(i+1).getOpen();
			slope[i] = graphClose - graphOpen;
		}
		return slope;
	}

}