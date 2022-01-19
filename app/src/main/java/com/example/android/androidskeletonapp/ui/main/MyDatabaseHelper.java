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

public class MyDatabaseHelper extends SQLiteOpenHelper{

    private Context context;
    private static final String DATABASE_NAME = "BluetoothDevices.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Temperatures";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DEVICE_NAME = "device_name";
    private static final String COLUMN_CURRENT_TEMP = "current_temperature";
    private static final String COLUMN_MAX_TEMP = "max_temperature";
    private static final String COLUMN_MIN_TEMP = "min_temperature";
    private static final String COLUMN_AVG_TEMP = "avg_temperature";
    private static final String COLUMN_CAPTURE_DATA = "capture_date";


    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICE_NAME + " TEXT, " +
                COLUMN_CURRENT_TEMP + " TEXT, " +
                COLUMN_MAX_TEMP + " TEXT, " +
                COLUMN_MIN_TEMP + " TEXT, " +
                COLUMN_AVG_TEMP + " TEXT, " +
                COLUMN_CAPTURE_DATA + " DATE);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addTemperatures(String deviceName, String currentTemp, String maxTemp, String minTemp, String avgTemp){
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

    //returns all data from database
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

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
