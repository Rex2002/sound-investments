package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class GeneralTrends {
    public List<AverageDayPrice> calculateMovingAverage(List<Price> priceList) {
        // Implementiere die Berechnung des gleitenden Durchschnitts (Moving Average)
        // Gib den Durchschnitt zu jedem Zeitpunkt als Liste von Double-Wert zurück

	List<AverageDayPrice> priceAverages = new ArrayList<>();
    List<Double> previousPrices = new ArrayList<>();

    for (Price price : priceList) {
        previousPrices.add((price.getlow() + price.gethigh() + price.getopen() + price.getclose()) / 4);

        if (previousPrices.size() > 200) {
            previousPrices.remove(0); // Entferne den ältesten Preis, wenn die Liste länger als 200 ist
        }

        double average = calculateAverage(previousPrices);
        Calendar date = price.getday();

        AverageDayPrice priceData = new AverageDayPrice(average, date);
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

	public Boolean[] AverageIntersectsStock(List<AverageDayPrice> priceAverages, List<Price> priceList){
		Boolean[] intersections = new Boolean[priceAverages.size()];
	
		 //Implementiert eine Funktion welche Testet, ob der Moving Average für einen Zeitpunkt gleich dem Stockpreis ist
		 //Gibt die Ergebnise als Liste von Booleans zurück

		for (int i = 0; i <= priceAverages.size(); i++) {
			double average = priceAverages.get(i).getAverage();
			double graphLow = priceList.get(i).getlow();
			double graphHigh = priceList.get(i).gethigh();
			if(average >= graphLow && average <= graphHigh){
				intersections[i] = true;	
			}
			else{
				intersections[i] = false;
			}
		}
	
		return intersections;
	}
	
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

	public Double[] calculateSlope(List<Price> priceList){
		Double[] Slope = new Double[priceList.size()];
		for (int i = 0; i <= priceList.size(); i++) {
			double graphClose = priceList.get(i).getclose();
			double graphOpen = priceList.get(i+1).getopen();
			Slope[i] = graphClose-graphOpen;
		}
		return Slope;
	}
	public Boolean[] GraphIntersectsMinMax(List<Price> priceList, List<Double> minMax){
		Boolean[] intersectsminMax = new Boolean[minMax.size()];
		double min = minMax.get(0);
		double max = minMax.get(1);
		for (int i = 0; i <= priceList.size(); i++) {
			double graphLow = priceList.get(i).getlow();
			double graphHigh = priceList.get(i).gethigh();
			if(min==graphLow|| max==graphHigh){
				intersectsminMax[i]=true;	
			}
			else{
				intersectsminMax[i]=false;
			}
		}
		return intersectsminMax;
	}
}
