package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class TriangleFormationAnalyzer {
    public static int MIN_FORMATION_LEN = 5;

    public boolean[] analyze(List<Price> priceList) {
        boolean[] triangleformations = new boolean[priceList.size()];
        double[] blurredValues = new double[priceList.size()];
		blurredValues = gaussianBlur(priceList);

        for (int i = 0; i < triangleformations.length; i++) {
            Price p1 = priceList.get(i);
            double lastHigh = p1.getHigh();
            double lastLow = p1.getLow();
            // Check for decreasing formation
            int formationLen = 0;
            for (int j = i + 1; j < triangleformations.length; j++, formationLen++) {
                double currentHigh = priceList.get(j).getHigh();
                double currentLow = priceList.get(j).getLow();
                if (currentHigh > lastHigh || currentLow < lastLow)
                    break;
                lastHigh = currentHigh;
                lastLow = currentLow;
            }

            // Check for increasing formation
            if (formationLen == 0) {
                for (int j = i + 1; j < triangleformations.length; j++, formationLen++) {
                    double currentHigh = priceList.get(j).getHigh();
                    double currentLow = priceList.get(j).getLow();
                    if (currentHigh < lastHigh || currentLow > lastLow)
                        break;
                    lastHigh = currentHigh;
                    lastLow = currentLow;
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