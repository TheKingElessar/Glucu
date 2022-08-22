package com.example.glucu.Database;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

public class InsertEGVS extends Thread {

    Context context;
    EGVSDao egvsDao;
    List<EGVS> egvsList;
    public static AppDatabase db;

    public InsertEGVS(Context context, List<EGVS> egvsList) {
        this.context = context;
        this.egvsList = egvsList;
    }

    public void run() {
        db = Room.databaseBuilder(this.context, AppDatabase.class, "EGVS_Database").build();
        egvsDao = db.getEgvsDao();

        egvsDao.insertList(egvsList);

    }
}