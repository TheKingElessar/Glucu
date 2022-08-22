package com.example.glucu.DisplayData;

import com.scichart.core.model.DateValues;

import java.util.Date;

public class DateSeries {

    public final DateValues xValues;
    public final DateValues yValues;

    public DateSeries(int capacity){
        xValues = new DateValues(capacity);
        yValues = new DateValues(capacity);
    }

    public void add(Date x, Date y){
        xValues.add(x);
        yValues.add(y);
    }
}
