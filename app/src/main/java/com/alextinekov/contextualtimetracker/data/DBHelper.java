package com.alextinekov.contextualtimetracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alex Tinekov on 01.06.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "last_used_apps";
    public static final String FIELD_PACKAGE = "package";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TIME = "time";

    public DBHelper(Context ctx){
        super(ctx, "CTTrackingDB", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "("
                +"id integer primary key autoincrement,"
                +FIELD_PACKAGE + " text,"
                +FIELD_NAME + " text, "
                +FIELD_TIME + " integer);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
