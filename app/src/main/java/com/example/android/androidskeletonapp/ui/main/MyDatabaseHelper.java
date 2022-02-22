package com.example.android.androidskeletonapp.ui.main;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Spliterator;
import java.util.UUID;

public class MyDatabaseHelper extends SQLiteOpenHelper{

    private Context context;
    private static final String DATABASE_NAME = "BluetoothDevices.db";
    private static final int DATABASE_VERSION = 2;

    //DB for directly DHIS2 push
    private static final String TABLE_NAME = "Temperatures";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DEVICE_NAME = "device_name";
    private static final String COLUMN_CURRENT_TEMP = "current_temperature";
    private static final String COLUMN_MAX_TEMP = "max_temperature";
    private static final String COLUMN_MIN_TEMP = "min_temperature";
    private static final String COLUMN_AVG_TEMP = "avg_temperature";
    private static final String COLUMN_CAPTURE_DATA = "capture_date";

    private static final String OFFLINE_TABLE_NAME = "Offline_captured_temp";
    private static final String OFFLINE_COLUMN_ID = "_id";
    private static final String OFFLINE_COLUMN_DEVICE_NAME = "device_name";
    private static final String OFFLINE_COLUMN_CURRENT_TEMP = "current_temperature";
    private static final String OFFLINE_COLUMN_MAX_TEMP = "max_temperature";
    private static final String OFFLINE_COLUMN_MIN_TEMP = "min_temperature";
    private static final String OFFLINE_COLUMN_AVG_TEMP = "avg_temperature";
    private static final String OFFLINE_COLUMN_CAPTURE_DATA = "capture_date";


    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String online_table = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_NAME + " TEXT, " +
                COLUMN_CURRENT_TEMP + " TEXT, " +
                COLUMN_MAX_TEMP + " TEXT, " +
                COLUMN_MIN_TEMP + " TEXT, " +
                COLUMN_AVG_TEMP + " TEXT, " +
                COLUMN_CAPTURE_DATA + " DATE);";

        String ofline_table = "CREATE TABLE " + OFFLINE_TABLE_NAME +
                " (" + OFFLINE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OFFLINE_COLUMN_DEVICE_NAME + " TEXT, " +
                OFFLINE_COLUMN_CURRENT_TEMP + " TEXT, " +
                OFFLINE_COLUMN_MAX_TEMP + " TEXT, " +
                OFFLINE_COLUMN_MIN_TEMP + " TEXT, " +
                OFFLINE_COLUMN_AVG_TEMP + " TEXT, " +
                OFFLINE_COLUMN_CAPTURE_DATA + " DATE);";

        db.execSQL(online_table);
        db.execSQL(ofline_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + OFFLINE_TABLE_NAME);
        onCreate(db);
    }

    public void addTemperatures(String deviceName, String currentTemp, String maxTemp, String minTemp, String avgTemp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String captureDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        cv.put(COLUMN_DEVICE_NAME, deviceName);
        cv.put(COLUMN_CURRENT_TEMP, currentTemp);
        cv.put(COLUMN_MAX_TEMP, maxTemp);
        cv.put(COLUMN_MIN_TEMP, minTemp);
        cv.put(COLUMN_AVG_TEMP, avgTemp);
        cv.put(COLUMN_CAPTURE_DATA, captureDate);

        long result = db.insert(TABLE_NAME,null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed to add temperatures", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }

    }
    public void addOfflineTemperatures(String deviceName, String currentTemp, String maxTemp, String minTemp, String avgTemp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String offline_captureDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        cv.put(OFFLINE_COLUMN_DEVICE_NAME, deviceName);
        cv.put(OFFLINE_COLUMN_CURRENT_TEMP, currentTemp);
        cv.put(OFFLINE_COLUMN_MAX_TEMP, maxTemp);
        cv.put(OFFLINE_COLUMN_MIN_TEMP, minTemp);
        cv.put(OFFLINE_COLUMN_AVG_TEMP, avgTemp);
        cv.put(OFFLINE_COLUMN_CAPTURE_DATA, offline_captureDate);

        long result = db.insert(OFFLINE_TABLE_NAME,null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed to add temperature", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Successfully added!", Toast.LENGTH_SHORT).show();
        }

    }

    //returns all data from database
    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    // TO-DO make it possible to check for duplicates on UUID
   /* public Boolean checkForDuplicates(String uuid){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + UNIQUE_ID + " =capture_uuid" ;
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()){
                if(cursor.getString(7).equals(uuid)){
                    return  true;
                }
            }
        }
        return false;
    };

    */


    public ArrayList<String> returnCurrentTemp(){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_CURRENT_TEMP + " =current_temperature" ;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(2));
            }
        }
        return Db_data;
    };

    public ArrayList<String> returnCaptureDate(){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_CAPTURE_DATA + " =capture_date" ;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(6));
            }
        }
        return Db_data;
    };

    // for online database
    public ArrayList<String> returnAvgTemps(){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +  COLUMN_AVG_TEMP + " =avg_temperature" + " AND " + COLUMN_MAX_TEMP + "=max_temperature" + " AND " + COLUMN_MIN_TEMP + "=min_temperature";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(2));
            }
        }
        return Db_data;
    };
    //For offline database
    public ArrayList<String> returnAvgTemps_offline(){
        String query = "SELECT * FROM " + OFFLINE_TABLE_NAME + " WHERE " +  OFFLINE_COLUMN_AVG_TEMP + " =avg_temperature" + " AND " + OFFLINE_COLUMN_MAX_TEMP + "=max_temperature" + " AND " + OFFLINE_COLUMN_MIN_TEMP + "=min_temperature";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(2));
            }
        }
        return Db_data;
    };

    //returns all data from online database
    public ArrayList<String> returnAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(0));
                Db_data.add(cursor.getString(1));
                Db_data.add(cursor.getString(2));
                Db_data.add(cursor.getString(3));
                Db_data.add(cursor.getString(4));
                Db_data.add(cursor.getString(5));
                Db_data.add(cursor.getString(6));
            }
        }
        return Db_data;
    }
    //returns all data from online database
    public ArrayList<String> returnAllOfflineData(){
        String query = "SELECT * FROM " + OFFLINE_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> Db_data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()){
                Db_data.add(cursor.getString(0));
                Db_data.add(cursor.getString(1));
                Db_data.add(cursor.getString(2));
                Db_data.add(cursor.getString(3));
                Db_data.add(cursor.getString(4));
                Db_data.add(cursor.getString(5));
                Db_data.add(cursor.getString(6));
            }
        }
        return Db_data;
    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
    void deleteAllOfflineData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + OFFLINE_TABLE_NAME);
    }
}
