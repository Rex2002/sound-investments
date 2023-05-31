package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import dataRepo.Price;

public class FlagFormationAnalyzer implements FormationAnalyzer {
	@Override
	public List<FormationResult> analyzeFormations(List<Price> priceList) {
		List<FormationResult> flagFormations = new ArrayList<>();
		for (int i = 1; i < priceList.size() - 1; i++) {
			Price currentDay = priceList.get(i);
			Calendar startDay = currentDay.getDay();
			Calendar endDay = null;
			int formationLength = 0;

			// Überprüfe, ob die aktuellen Tageswerte die Bedingungen für eine
			// Flaggen-Formation erfüllen
			if (currentDay.getClose() > priceList.get(i - 1).getClose()
					&& currentDay.getClose() > priceList.get(i + 1).getClose()
					&& currentDay.getHigh() > priceList.get(i - 1).getHigh()
					&& currentDay.getHigh() > priceList.get(i + 1).getHigh()
					&& currentDay.getLow() < priceList.get(i - 1).getLow()
					&& currentDay.getLow() < priceList.get(i + 1).getLow()) {

				// Aufrufen von TimeSpan stattdessen??
				// Überprüfe, wie lange die Formation anhält (von 3 bis 60 Tagen)
				int j = i - 1;
				while (j >= 0 && currentDay.getClose() > priceList.get(j).getClose()
						&& currentDay.getHigh() > priceList.get(j).getHigh()
						&& currentDay.getLow() < priceList.get(j).getLow()
						&& i - j <= 60) {
					formationLength++;
					startDay = priceList.get(j).getDay();
					j--;
				}
				j = i + 1;
				while (j < priceList.size() && currentDay.getClose() > priceList.get(j).getClose()
						&& currentDay.getHigh() > priceList.get(j).getHigh()
						&& currentDay.getLow() < priceList.get(j).getLow()
						&& j - i <= 60) {
					formationLength++;
					endDay = priceList.get(j).getDay();
					j++;
				}
			}

			if (formationLength >= 2 && formationLength <= 60) {
				int duration = formationLength + 1; // Zeitraum der Formation
				String namingShit = new String("Flag");
				FormationResult result = new FormationResult(startDay, endDay, duration, namingShit);
				flagFormations.add(result);
			}
		}

		return flagFormations;
	}
}
