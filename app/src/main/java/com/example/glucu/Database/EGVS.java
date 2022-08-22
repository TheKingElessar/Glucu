package com.example.glucu.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class EGVS implements Comparable<EGVS> {

    public EGVS(){
    }

    public EGVS(Date date, double valueEGVS, String trendEGVS) {
        this.systemTimeEGVS = date;
        this.valueEGVS = valueEGVS;
        this.trendEGVS = trendEGVS;
    }

    @PrimaryKey
    public Date systemTimeEGVS;

    @ColumnInfo(name = "value_egvs")
    public double valueEGVS;

    @ColumnInfo(name = "trend_egvs")
    public String trendEGVS;

    public Date getDateTime(){
        return systemTimeEGVS;
    }

    @Override
    public int compareTo(EGVS egvs) {
        return getDateTime().compareTo(egvs.getDateTime());
    }

}