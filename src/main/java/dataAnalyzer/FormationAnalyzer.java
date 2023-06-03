package dataAnalyzer;

import java.util.List;

import dataRepo.Price;

public interface FormationAnalyzer {
    List<FormationResult> analyzeFormations(List<Price> prices);
}
