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

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getFormationName() {
        return formationName;
    }

    public void setFormationName(String formationName) {
        this.formationName = formationName;
    }
    
    public Calendar getStartDate() {
        return startDate;
    }

}
