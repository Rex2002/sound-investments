package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class TriangleFormationAnalyzer implements FormationAnalyzer {
    @Override
    public List<Boolean> analyzeFormations(List<Price> priceList) {
        // Implementiere die Dreiecks-Formation Analyse
        // Gib eine Liste von Booleans zurück, die zu jedem Zeitpunkt angibt, ob eine Dreiecks-Formation vorliegt
    }
	@Override
	public int timeSpanFormation(List<Price> priceList){
		 // Implementiert die Mindest- und Maximalzeit in welcher eine Formation gilt
		 // Gibt eine Zeitspanne für die Erkannte Formation an
		 // Wenn eine Formation innerhalb einer weiteren Formation auftritt, wird die Formation ausgewählt, welche über einen längeren Zeitraum erkannt wurde.
	}
}