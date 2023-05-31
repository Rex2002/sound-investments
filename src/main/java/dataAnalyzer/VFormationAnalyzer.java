package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import dataRepo.Price;

public class VFormationAnalyzer implements FormationAnalyzer {
    @Override
    public List<FormationResult> analyzeFormations(List<Price> priceList) {
        // Implementiere die V-Formation Analyse
        // Gib eine Liste von Booleans zurück, die zu jedem Zeitpunkt angibt, ob eine
        // V-Formation vorliegt

        List<FormationResult> Vformations = new ArrayList<>();

        for (int i = 0; i < priceList.size(); i++) {
            Price startPrice = priceList.get(i);
            double startClose = startPrice.getClose();
            Calendar startDate = startPrice.getDay();

            for (int j = i + 1; j < priceList.size(); j++) {
                Price endPrice = priceList.get(j);
                double endClose = endPrice.getClose();
                Calendar endDate = endPrice.getDay();

                // Überprüfen, ob ein dynamischer Abwärtsimpuls stattfindet
                boolean downwardImpulse = false;
                for (int k = i + 1; k < j; k++) {
                    Price intermediatePrice = priceList.get(k);
                    double intermediateClose = intermediatePrice.getClose();

                    if (intermediateClose < startClose) {
                        downwardImpulse = true;
                        break;
                    }
                }

                if (downwardImpulse && endClose > startClose) {
                    // V-Formation gefunden
                    Vformations.add(new FormationResult(startDate, endDate, j - i, "V-Formation"));
                } else if (!downwardImpulse && endClose < startClose) {
                    // Inverse V-Formation gefunden
                    Vformations.add(new FormationResult(startDate, endDate, j - i, "Inverse V-Formation"));
                }
            }
        }

        return Vformations;

    }
}