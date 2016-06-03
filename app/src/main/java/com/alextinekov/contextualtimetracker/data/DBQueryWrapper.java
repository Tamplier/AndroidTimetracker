package com.alextinekov.contextualtimetracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Tinekov on 01.06.2016.
 */
public class DBQueryWrapper {
    private static DBQueryWrapper instance;
    private DBHelper helper;
    private int mOpenCounter;
    private SQLiteDatabase mDatabase;
    private DBQueryWrapper(Context ctx){
        helper = new DBHelper(ctx);
        mOpenCounter = 0;
    }

    public static synchronized DBQueryWrapper getInstance(Context ctx){
        if(instance == null){
            instance = new DBQueryWrapper(ctx);
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = helper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }
    public static void insertAppInfo(SQLiteDatabase db, RunnedApplicationInfo infos[]){
        ContentValues values = new ContentValues();
        db.beginTransaction();
            for(RunnedApplicationInfo info : infos){
                if(info != null) {
                    values.put(DBHelper.FIELD_PACKAGE, info.packageName);
                    values.put(DBHelper.FIELD_NAME, info.name);
                    values.put(DBHelper.FIELD_TIME, info.activeTime);
                    db.insert(DBHelper.TABLE_NAME, null, values);
                }
            }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public static List<RunnedApplicationInfo> getAppInfo(SQLiteDatabase db){
        Cursor c = db.rawQuery("select * from " + DBHelper.TABLE_NAME, null);
        ArrayList<RunnedApplicationInfo> infos = new ArrayList<RunnedApplicationInfo>();
        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                RunnedApplicationInfo info = new RunnedApplicationInfo();
                info.packageName = c.getString(c.getColumnIndex(DBHelper.FIELD_PACKAGE));
                info.name = c.getString(c.getColumnIndex(DBHelper.FIELD_NAME));
                info.activeTime = c.getLong(c.getColumnIndex(DBHelper.FIELD_TIME));
                infos.add(info);
                c.moveToNext();
            }
        }
        return infos;
    }

    public static void updateInfo(SQLiteDatabase db, RunnedApplicationInfo infos[]){
        clearInfoTable(db);
        insertAppInfo(db, infos);
    }

    public static void clearInfoTable(SQLiteDatabase db){
        db.execSQL("delete from " + DBHelper.TABLE_NAME);
    }
}
