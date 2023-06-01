package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class FlagFormationAnalyzer {
	public static boolean[] analyze(List<Price> priceList) {
		boolean[] out = new boolean[priceList.size()];
		out[0] = false;

		for (int i = 1; i < priceList.size() - 1; i++) {
			Price currentDay = priceList.get(i);
			int formationLength = 0;

			// Überprüfe, ob die aktuellen Tageswerte die Bedingungen für eine Flaggen-Formation erfüllen
			if (currentDay.getClose() > priceList.get(i - 1).getClose()
				&& currentDay.getClose() > priceList.get(i + 1).getClose()
				&& currentDay.getHigh() > priceList.get(i - 1).getHigh()
				&& currentDay.getHigh() > priceList.get(i + 1).getHigh()
				&& currentDay.getLow() < priceList.get(i - 1).getLow()
				&& currentDay.getLow() < priceList.get(i + 1).getLow()) {

				// Überprüfe, wie lange die Formation anhält (von 3 bis 60 Tagen)
				int j = i - 1;
				while (j >= 0 && currentDay.getClose() > priceList.get(j).getClose()
					&& currentDay.getHigh() > priceList.get(j).getHigh()
					&& currentDay.getLow() < priceList.get(j).getLow()
					&& i - j <= 60) {
						formationLength++;
						j--;
				}
				j = i + 1;
				while (j < priceList.size() && currentDay.getClose() > priceList.get(j).getClose()
					&& currentDay.getHigh() > priceList.get(j).getHigh()
					&& currentDay.getLow() < priceList.get(j).getLow()
					&& j - i <= 60) {
						formationLength++;
						j++;
				}
			}

			if (formationLength >= 2 && formationLength <= 60) {
				for (int k = 0; k < formationLength; k++) {
					out[i-k] = true;
				}
			} else {
				out[i] = false;
			}
		}

		return out;
	}
}
