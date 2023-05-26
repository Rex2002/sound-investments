package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;

public interface FormationAnalyzer {
    List<FormationResult> analyzeFormations(List<Price> prices);
	int timeSpanFormation(List<Price> prices);
}
