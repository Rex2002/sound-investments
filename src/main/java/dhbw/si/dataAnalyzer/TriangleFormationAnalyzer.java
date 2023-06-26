package dhbw.si.dataAnalyzer;

import java.util.List;
import dhbw.si.dataRepo.Price;

/**
 * @author J. Kautz
 */

public class TriangleFormationAnalyzer {
    public static final int MIN_FORMATION_LEN = 5;

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
        for (int j = 0; j <= formationLen+2; j++) {
          triangleformations[i-j] = true;
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