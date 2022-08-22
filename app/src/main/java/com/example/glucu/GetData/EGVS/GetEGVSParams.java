package com.example.glucu.GetData.EGVS;

import android.content.Context;

public class GetEGVSParams {
    Context context;
    String START_DATE;
    String END_DATE;

    public GetEGVSParams(Context context, String START_DATE, String END_DATE) {
        this.context = context;
        this.START_DATE = START_DATE;
        this.END_DATE = END_DATE;
    }

    static public String convertToDate(String year, String month, String day, String hour, String minute, String second) {
        String dateSeparator = "-";
        String timeSeparator = ":";

        String date = year + dateSeparator + month + dateSeparator + day + "T" + hour + timeSeparator + minute + timeSeparator + second;

        return date;
    }
}
