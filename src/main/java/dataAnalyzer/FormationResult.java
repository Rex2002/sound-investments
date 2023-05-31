package dataAnalyzer;

import java.util.Calendar;

public class FormationResult {
    private Calendar startDate;
    private Calendar endDate;
    private long duration;
    private String formationName;

    public FormationResult(Calendar startDate, Calendar endDate, long duration, String formationName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.formationName = formationName;
    }
    
        public Calendar getStartDate() {
            return startDate;
        }
    
        public Calendar getEndDate() {
            return endDate;
        }
    
        public long getDuration() {
            return duration;
        }
    
        public String getFormationName() {
            return formationName;
        }
}
