package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class MovingAverage implements TrendAnalyzer{
    @Override
    public List<PriceData> calculateMovingAverage(List<Price> priceList) {
        // Implementiere die Berechnung des gleitenden Durchschnitts (Moving Average)
        // Gib den Durchschnitt zu jedem Zeitpunkt als Liste von Double-Wert zurück

	List<PriceData> priceAverages = new ArrayList<>();
    List<Double> previousPrices = new ArrayList<>();

    for (Price price : priceList) {
        previousPrices.add((price.getlow() + price.gethigh() + price.getopen() + price.getclose()) / 4);

        if (previousPrices.size() > 200) {
            previousPrices.remove(0); // Entferne den ältesten Preis, wenn die Liste länger als 200 ist
        }

        double average = calculateAverage(previousPrices);
        Calendar date = price.getday();

        PriceData priceData = new PriceData(average, date);
        priceAverages.add(priceData);
    }

    return priceAverages;
}

	private double calculateAverage(List<Double> prices) {
    	double sum = 0.0;
    	for (double price : prices) {
        	sum += price;
    	}
    	return sum / prices.size();
	}


	@Override
	public List<Boolean> AverageIntersectsStock(List<PriceData> priceAverages, List<Price> priceList){
		List<Boolean> intersections = new ArrayList<>();
	
		 //Implementiert eine Funktion welche Testet, ob der Moving Average für einen Zeitpunkt gleich dem Stockpreis ist
		 //Gibt die Ergebnise als Liste von Booleans zurück

		for (int i = 0; i <= priceAverages.size(); i++) {
			double average = priceAverages.get(i).getAverage();
			double graphLow = priceList.get(i).getlow();
			double graphHigh = priceList.get(i).gethigh();
			double graphOpen = priceList.get(i).getopen();
			double graphClose = priceList.get(i).getclose();
	
			
			if(average==graphLow|| average == graphHigh|| average==graphClose|| average==graphOpen){
				boolean intersects = true;
				intersections.add(intersects);	
			}
		}
	
		return intersections;
	}
	@Override
	public List<Double> calculateMinAndMax(List<Price> priceList){
		List<Double> minMax = new ArrayList<>();
		//Implementiert die Erkennung von lokalen Minima/Maxima
		//Vergleicht alle gegebenen Min/Max Werte
		//Gibt die jeweils höchsten(/niedrigsten) Werte als Liste von doubles aus
		//Maximum gesamt = Widerstand
		//Minimum gesamt = Unterstützung
		double max = Double.MAX_VALUE;
		double min = Double.MIN_VALUE;
	
		for (Price price : priceList) {
			if (price.getlow() < min) {
				min = price.getlow();
			}
	
			if (price.gethigh() > max) {
				max = price.gethigh();
			}
		}
		minMax.add(min);
		minMax.add(max);
		return minMax;
	
	}

	@Override
	public List<Boolean> GraphIntersectsMinMax(List<Price> priceList, List<Double> minMax){
		List<Boolean> intersectsminMax = new ArrayList<>();
		double min = minMax.get(0);
		double max = minMax.get(1);
		for (int i = 0; i <= priceList.size(); i++) {
			double graphLow = priceList.get(i).getlow();
			double graphHigh = priceList.get(i).gethigh();
			double graphOpen = priceList.get(i).getopen();
			double graphClose = priceList.get(i).getclose();
			if(min==graphLow|| min==graphClose|| min==graphOpen ||max==graphHigh|| max==graphClose|| max==graphOpen){
				boolean intersects = true;
				intersectsminMax.add(intersects);	
			}
		}
		return intersectsminMax;
	}
}
