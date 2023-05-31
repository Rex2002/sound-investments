package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import dataRepo.Price;

public class TriangleFormationAnalyzer implements FormationAnalyzer {
    @Override
    public List<FormationResult> analyzeFormations(List<Price> priceList) {
        List<FormationResult> triangleformations = new ArrayList<>();

        for (int i = 0; i < priceList.size(); i++) {
            Price startPrice = priceList.get(i);
            double startOpen = startPrice.getOpen();
            double startClose = startPrice.getClose();
            double startHigh = startPrice.getHigh();
            double startLow = startPrice.getLow();
            Calendar startDate = startPrice.getDay();

            for (int j = i + 1; j < priceList.size(); j++) {
                Price endPrice = priceList.get(j);
                double endOpen = endPrice.getOpen();
                double endClose = endPrice.getClose();
                double endHigh = endPrice.getHigh();
                double endLow = endPrice.getLow();
                Calendar endDate = endPrice.getDay();

                if (endHigh < startHigh && endLow > startLow) {
                    // Überprüfen, ob die Handelsspanne zwischen oberer und unterer Begrenzung sich
                    // sukzessive verkleinert
                    boolean decreasingRange = true;
                    for (int k = i + 1; k < j; k++) {
                        Price intermediatePrice = priceList.get(k);
                        double intermediateHigh = intermediatePrice.getHigh();
                        double intermediateLow = intermediatePrice.getLow();

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

                            double intermediateClose1 = intermediatePrice1.getClose();
                            double intermediateClose2 = intermediatePrice2.getClose();

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