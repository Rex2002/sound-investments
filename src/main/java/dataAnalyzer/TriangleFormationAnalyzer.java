package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class TriangleFormationAnalyzer implements FormationAnalyzer {
    @Override
    public List<FormationResult> analyzeFormations(List<Price> priceList) {
        List<FormationResult> triangleformations = new ArrayList<>();

        for (int i = 0; i < priceList.size(); i++) {
            Price startPrice = priceList.get(i);
            double startOpen = startPrice.getopen();
            double startClose = startPrice.getclose();
            double startHigh = startPrice.gethigh();
            double startLow = startPrice.getlow();
            Calendar startDate = startPrice.getday();

            for (int j = i + 1; j < priceList.size(); j++) {
                Price endPrice = priceList.get(j);
                double endOpen = endPrice.getopen();
                double endClose = endPrice.getclose();
                double endHigh = endPrice.gethigh();
                double endLow = endPrice.getlow();
                Calendar endDate = endPrice.getday();

                if (endHigh < startHigh && endLow > startLow) {
                    // Überprüfen, ob die Handelsspanne zwischen oberer und unterer Begrenzung sich sukzessive verkleinert
                    boolean decreasingRange = true;
                    for (int k = i + 1; k < j; k++) {
                        Price intermediatePrice = priceList.get(k);
                        double intermediateHigh = intermediatePrice.gethigh();
                        double intermediateLow = intermediatePrice.getlow();

                        if (intermediateHigh >= startHigh || intermediateLow <= startLow) {
                            decreasingRange = false;
                            break;
                        }
                    }

                    if (decreasingRange) {
                        // Überprüfen, ob eine der Trendlinien einen stärkeren Neigungswinkel hat
                        boolean increasingTrend = false;
                        boolean decreasingTrend = false;

                        for (int k = i + 1; k < j; k++) {
                            Price intermediatePrice1 = priceList.get(k);
                            Price intermediatePrice2 = priceList.get(k + 1);

                            double intermediateClose1 = intermediatePrice1.getclose();
                            double intermediateClose2 = intermediatePrice2.getclose();

                            if (intermediateClose2 > intermediateClose1) {
                                increasingTrend = true;
                            } else if (intermediateClose2 < intermediateClose1) {
                                decreasingTrend = true;
                            }

                            if (increasingTrend && decreasingTrend) {
                                break;
                            }
                        }

                        if (increasingTrend && !decreasingTrend) {
                            triangleformations.add(new FormationResult(startDate, endDate, j - i, "Triangle"));
                        } else if (!increasingTrend && decreasingTrend) {
                            triangleformations.add(new FormationResult(startDate, endDate, j - i, "Triangle"));
                        }
                    }
                }
            }
        }

        return triangleformations;

    }
}