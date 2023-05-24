package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;

public interface FormationAnalyzer {
    List<Boolean> analyzeFormations(List<Price> prices);
	int timeSpanFormation(List<Price> prices);
}
