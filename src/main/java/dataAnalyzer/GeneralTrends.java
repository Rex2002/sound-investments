package dataAnalyzer;

import dataRepo.Price;

import java.util.ArrayList;
import java.util.List;

public class GeneralTrends {
	public static double[] calculateMovingAverage(List<Price> priceList) {
		// Implementiere die Berechnung des gleitenden Durchschnitts (Moving Average)
		// Gib den Durchschnitt zu jedem Zeitpunkt als Liste von Double-Wert zurück

		double[] priceAverages = new double[priceList.size()];
		double[] previousPrices = new double[200];

		for (int i = 0; i < priceList.size(); i++) {
			Price price = priceList.get(i);
			previousPrices[i % previousPrices.length] = (price.getLow() + price.getHigh() + price.getOpen() + price.getClose()) / 4;
			priceAverages[i] = calculateAverage(previousPrices, i > previousPrices.length ? previousPrices.length : i);
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

	public static boolean[] GraphIntersectsMinMax(List<Price> priceList, List<Double> minMax){
		boolean[] intersectsminMax = new boolean[minMax.size()];
		double min = minMax.get(0);
		double max = minMax.get(1);
		for (int i = 0; i <= priceList.size(); i++) {
			Price p = priceList.get(i);
			intersectsminMax[i] = (p.getHigh() >= min && p.getLow() <= min) || (p.getHigh() >= max && p.getLow() <= max);
		}
		return intersectsminMax;
	}
}