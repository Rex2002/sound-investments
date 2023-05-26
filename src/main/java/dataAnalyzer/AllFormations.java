package dataAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class AllFormations {
    public List<Formations> decideOnFormations(List<FormationResult> flagFormations,
                                                List<FormationResult> TriangleFormations,
                                                List<FormationResult> VFormations){
        List<Formations> overlappingFormations = new ArrayList<>();
        // Iteriere über alle Formationen der Flaggen-Formation
        for (FormationResult flagFormation : flagFormations) {
            Formations overlappingFormation = getOverlappingFormation(flagFormation, TriangleFormations, VFormations);
            if (overlappingFormation != null) {
                overlappingFormations.add(overlappingFormation);
            }
        }
                                            
        // Iteriere über alle Formationen der Dreiecks-Formation
        for (FormationResult triangleFormation : TriangleFormations) {
            Formations overlappingFormation = getOverlappingFormation(triangleFormation, flagFormations, VFormations);
            if (overlappingFormation != null) {
                overlappingFormations.add(overlappingFormation);
            }
        }
                                            
        // Iteriere über alle Formationen der V-Formation
        for (FormationResult vFormation : VFormations) {
            Formations overlappingFormation = getOverlappingFormation(vFormation, flagFormations, TriangleFormations);
            if (overlappingFormation != null) {
                overlappingFormations.add(overlappingFormation);
            }
        }
                                            
        return overlappingFormations;
    }

    private static Formations getOverlappingFormation(FormationResult formation,
                                                         List<FormationResult> formationList1,
                                                         List<FormationResult> formationList2) {
        Formations overlappingFormation = null;

        for (FormationResult otherFormation : formationList1) {
            if (areFormationsOverlapping(formation, otherFormation)) {
                if (overlappingFormation == null || formation.getDuration() > overlappingFormation.getDuration()) {
                    overlappingFormation = createFormationData(formation);
                }
            }
        }

        for (FormationResult otherFormation : formationList2) {
            if (areFormationsOverlapping(formation, otherFormation)) {
                if (overlappingFormation == null || formation.getDuration() > overlappingFormation.getDuration()) {
                    overlappingFormation = createFormationData(formation);
                }
            }
        }

        return overlappingFormation;
    }

    private static boolean areFormationsOverlapping(FormationResult formation1, FormationResult formation2) {
        Calendar start1 = formation1.getStartDate();
        Calendar end1 = formation1.getEndDate();
        Calendar start2 = formation2.getStartDate();
        Calendar end2 = formation2.getEndDate();

        // Überprüfe, ob die Formationen am selben Tag liegen
        if (start1.get(Calendar.YEAR) == start2.get(Calendar.YEAR) &&
                start1.get(Calendar.MONTH) == start2.get(Calendar.MONTH) &&
                start1.get(Calendar.DAY_OF_MONTH) == start2.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }

        // Überprüfe, ob die Formationen sich überschneiden
        return start1.before(end2) && end1.after(start2);
    }

    private static Formations createFormationData(FormationResult formation) {
        Formations formationData = new Formations();
        formationData.setStartDate(formation.getStartDate());
        formationData.setEndDate(formation.getEndDate());
        formationData.setDuration(formation.getDuration());
        formationData.setFormationName(formation.getFormationName());
        return formationData;
    }
}


