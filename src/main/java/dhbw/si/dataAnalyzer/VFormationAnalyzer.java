package dhbw.si.dataAnalyzer;

import java.util.List;

import dhbw.si.dataRepo.Price;

public class VFormationAnalyzer {
    public static boolean[] analyze(List<Price> priceList) {
        // Implementiere die V-Formation Analyse
        // Gib eine Liste von Booleans zurück, die zu jedem Zeitpunkt angibt, ob eine
        // V-Formation vorliegt

		double[] input = Blur.averageBlur(priceList);
        int length = input.length;
        boolean[] Vformations = new boolean[length];
        int formationLength = 0;
        boolean downwardImpulse = false;
        boolean isFlag = false;
        boolean TrendDone = false;

        for (int i = 1; i < length; i++) {
        double startPrice = input[i - 1];
        double nextPrice = input[i];
        // Überprüfen, ob ein dynamischer Abwärtsimpuls stattfindet
        // Price intermediatePrice = priceList.get(k);
        if (nextPrice < startPrice&&!isFlag) {
            downwardImpulse = true;
            formationLength++;
        } else if (nextPrice > startPrice && downwardImpulse) {
            formationLength++;
            downwardImpulse = false;
            isFlag = true;
        } else if (nextPrice > startPrice && isFlag) {
            formationLength++;
        }
        if (nextPrice <= startPrice && isFlag) {
            TrendDone = true;
            isFlag = false;
        }

        // V-Formation gefunden
        if (TrendDone && formationLength >= 5 && formationLength <= 90) {
            for (int k = 0; k < formationLength; k++) {
            Vformations[i - k-1] = true;
            }
            formationLength = 0;
            
        } else {
            Vformations[i] = false;
        }
        }
        return Vformations;

    }
}