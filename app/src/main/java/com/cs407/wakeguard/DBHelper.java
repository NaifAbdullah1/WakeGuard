package com.cs407.wakeguard;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBHelper {

    static SQLiteDatabase sqLiteDatabase;

    public DBHelper (SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public static void createTable(){
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes " +
                "(id INTEGER PRIMARY KEY, " +
                "alarmId INTEGER, " +
                "title TEXT, " +
                "alarmToneName TEXT, " +
                "isVibrationEnabled INTEGER, " +
                "isWakeGuardEnabled INTEGER, " +
                "time TEXT, " +
                "isActive INTEGER, " +
                "daysActive TEXT)"); // comma-separated list -- 'Monday', 'Tuesday', etc.
    }

    public void saveNotes(String username, String title, String date, String body){
        createTable();

    }

    public void updateNotes(String body, String date, String title, String username){
        createTable();

    }

    public void deleteNotes(String body, String title){
        createTable();

    }
}
