package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class MovingAverage {
    @Override
    public List<Double> calculateMovingAverage(List<Price> priceList) {
        // Implementiere die Berechnung des gleitenden Durchschnitts (Moving Average)
        // Gib den Durchschnitt zu jedem Zeitpunkt als Liste von Double-Wert zurück
    }
	@Override
	List<Boolean> averageIntersectsStock(List<Price> priceList){
		 //Implementiert eine Funktion welche Testet, ob der Moving Average für einen Zeitpunkt gleich dem Stockpreis ist
		 //Gibt die Ergebnise als Liste von Booleans zurück
	}
	@Override
	List<Double> calculateMinAndMax(List<Price> prices){
		//Implementiert die Erkennung von lokalen Minima/Maxima
		//Vergleicht alle gegebenen Min/Max Werte
		//Gibt die jeweils höchsten(/niedrigsten) Werte als Liste von doubles aus
		//Maximum gesamt = Widerstand
		//Minimum gesamt = Unterstützung
	}
}
