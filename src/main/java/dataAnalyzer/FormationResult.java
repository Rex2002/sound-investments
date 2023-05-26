package dataAnalyzer;

import java.util.Calendar;

public class FormationResult {
        private Calendar startDay;
        private Calendar endDay;
        private int duration;
    
        public FormationResult(Calendar startDay, Calendar endDay, int duration) {
            this.startDay = startDay;
            this.endDay = endDay;
            this.duration = duration;
        }
}
