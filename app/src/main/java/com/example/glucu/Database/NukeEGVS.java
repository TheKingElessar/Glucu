package com.example.glucu.Database;

import android.content.Context;

import androidx.room.Room;

public class NukeEGVS extends Thread {

    private Context context;

    public NukeEGVS(Context context) {
        this.context = context;
    }

    public void run() {
        AppDatabase db = Room.databaseBuilder(this.context, AppDatabase.class, "EGVS_Database").build();
        db.clearAllTables();
        System.out.println("Table nuked!");
    }
}