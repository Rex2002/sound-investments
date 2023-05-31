package dataAnalyzer;

import java.util.List;

public interface FormationAnalyzer {
    List<FormationResult> analyzeFormations(List<Price> prices);
}
