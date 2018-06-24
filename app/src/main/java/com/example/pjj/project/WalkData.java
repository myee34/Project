package com.example.pjj.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WalkData extends SQLiteOpenHelper {
    public WalkData(Context context) {
        super(context, "DataBase", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WalkHistory (_id INTEGER PRIMARY KEY AUTOINCREMENT, walk_cnt int(20), Testdate date(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WalkHistory");
        onCreate(db);
    }
}