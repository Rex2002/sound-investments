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

					// Überprüfe, ob die aktuellen Tageswerte die Bedingungen für eine Flaggen-Formation erfüllen
					if (currentDay.getclose() > priceList.get(i - 1).getclose()
						&& currentDay.getclose() > priceList.get(i + 1).getclose()
						&& currentDay.gethigh() > priceList.get(i - 1).gethigh()
						&& currentDay.gethigh() > priceList.get(i + 1).gethigh()
						&& currentDay.getlow() < priceList.get(i - 1).getlow()
						&& currentDay.getlow() < priceList.get(i + 1).getlow()) {

						//Aufrufen von TimeSpan stattdessen??
						// Überprüfe, wie lange die Formation anhält (von 3 bis 60 Tagen)
						int j = i - 1;
						while (j >= 0 && currentDay.getclose() > priceList.get(j).getclose()
							&& currentDay.gethigh() > priceList.get(j).gethigh()
							&& currentDay.getlow() < priceList.get(j).getlow()
							&& i - j <= 60) {
								formationLength++;
								startDay = priceList.get(j).getday();
								j--;
						}
						j = i + 1;
						while (j < priceList.size() && currentDay.getclose() > priceList.get(j).getclose()
							&& currentDay.gethigh() > priceList.get(j).gethigh()
							&& currentDay.getlow() < priceList.get(j).getlow()
							&& j - i <= 60) {
								formationLength++;
								endDay = priceList.get(j).getday();
								j++;
						}
					}

					if (formationLength >= 2 && formationLength <= 60) {
						int duration = formationLength + 1; // Zeitraum der Formation
						String namingFormation = new String("Flag");
						FormationResult result = new FormationResult(startDay, endDay, duration, namingFormation);
						flagFormations.add(result);
					}
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
