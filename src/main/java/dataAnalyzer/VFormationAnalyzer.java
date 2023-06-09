package dataAnalyzer;

import java.util.List;

import dataRepo.Price;

public class VFormationAnalyzer {
    public boolean[] analyze(List<Price> priceList) {

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
        if (nextPrice+1.0 < startPrice) {
            downwardImpulse = true;
            formationLength++;
        } else if (nextPrice > startPrice+1.0 && downwardImpulse) {
            formationLength++;
            downwardImpulse = false;
            isFlag = true;
        } else if (nextPrice > startPrice+0.5 && isFlag) {
            formationLength++;
        }
        if (nextPrice < startPrice && isFlag) {
            TrendDone = true;
        }

        // V-Formation gefunden
        if (TrendDone && isFlag && formationLength >= 5 && formationLength <= 90) {
            for (int k = 0; k < formationLength; k++) {
            Vformations[i - k] = true;
            }
            formationLength = 0;
            isFlag = false;
        } else {
            Vformations[i] = false;
        }
        }
        return Vformations;

    }
}