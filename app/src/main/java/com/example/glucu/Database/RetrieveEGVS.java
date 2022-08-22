package com.example.glucu.Database;

import android.content.Context;

import androidx.room.Room;

import java.io.File;
import java.util.List;

public class RetrieveEGVS extends Thread {

    Context context;
    EGVSDao egvsDao;
    AppDatabase db;
    public List<EGVS> egvsList;

    public RetrieveEGVS(Context context, AppDatabase db) {
        this.context = context;
        this.db = db;
    }

    public void run() {
        db = Room.databaseBuilder(this.context, AppDatabase.class, "EGVS_Database").build();

        egvsDao = db.getEgvsDao();

        egvsList = egvsDao.getAll();
    }
}