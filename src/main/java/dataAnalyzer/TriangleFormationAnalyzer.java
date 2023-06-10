package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class TriangleFormationAnalyzer {
    public static int MIN_FORMATION_LEN = 5;

    public static boolean[] analyze(List<Price> priceList) {
        boolean[] triangleformations = new boolean[priceList.size()];
        double[] blurredValues = Blur.averageBlur(priceList);

        double compDif = Math.abs(blurredValues[0] - blurredValues[1]);
    double maxDifference = 1.0;
    int formationLen = 0;
    boolean end = false;

    for (int i = 2; i < blurredValues.length; i++) {
      double currentDifference = Math.abs(blurredValues[i] - blurredValues[i - 1]);
      if (currentDifference > maxDifference && currentDifference < compDif) {
        formationLen++;
      } else if (currentDifference <= maxDifference && currentDifference < compDif) {
        end = true;
      }
      if (end&&MIN_FORMATION_LEN<=formationLen) {
        for (int j = 0; j <= formationLen; j++) {
          triangleformations[i - j] = true;
          triangleformations[i - j - 1] = true;
          triangleformations[i - j - 2] = true;
        }
        end = false;
        formationLen = 0;
      } else {
        triangleformations[i] = false;
      }
      compDif = currentDifference;
    }

    return triangleformations;
    }
}