package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class TriangleFormationAnalyzer {
    public static int MIN_FORMATION_LEN = 5;

    public boolean[] analyze(List<Price> priceList) {
        boolean[] triangleformations = new boolean[priceList.size()];
        double[] blurredValues = Blur.averageBlur(priceList);

        for (int i = 0; i < triangleformations.length; i++) {
            double lastPrice = blurredValues[i];

            // Check for decreasing formation
            int formationLen = 0;
            for (int j = i + 1; j < triangleformations.length; j++, formationLen++) {
                double currentPrice = blurredValues[j];
                if (currentPrice > lastPrice)
                    break;
                lastPrice = currentPrice;
            }

            // Check for increasing formation
            if (formationLen == 0) {
                for (int j = i + 1; j < triangleformations.length; j++, formationLen++) {
                    double currentPrice = blurredValues[j];
                    if (currentPrice < lastPrice)
                        {break;}
                    lastPrice = currentPrice;
                }
            }

            if (formationLen >= MIN_FORMATION_LEN) {
                for (int k = i; k < i + formationLen; k++) {
                    triangleformations[k] = true;
                }
                i += formationLen;
            } else {
                triangleformations[i] = false;
            }
        }

        return triangleformations;
    }
}