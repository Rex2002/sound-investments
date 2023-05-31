package dataAnalyzer;

import java.util.Calendar;

public class PriceData {
    private double average;
    private Calendar date;


    public PriceData(double average, Calendar date) {
        this.average = average;
        this.date = date;
    }

    public double getAverage(){
        return average;
    }

    public Calendar getDate(){
        return date;
    }

}
