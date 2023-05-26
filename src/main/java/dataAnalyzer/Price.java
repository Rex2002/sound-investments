package dataAnalyzer;

import java.time.Instant;
import java.util.Calendar;

public class Price {
    private Calendar day;
	private Instant start;
	private Instant end;
	private double open;
	private double close;
	private double low;
	private double high;

	public double getclose() {
        return close;
    }
	public double gethigh(){
		return high;
	}
	public double getlow(){
		return low;
	}
	public double getopen(){
		return open;
	}
	public Calendar getday(){
		return day;
	}
	public Instant getstart(){
		return start;
	}
	public Instand getend(){
		return end;
	}
}
