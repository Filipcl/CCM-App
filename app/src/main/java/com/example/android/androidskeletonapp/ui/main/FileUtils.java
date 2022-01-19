package com.example.android.androidskeletonapp.ui.main;

import android.content.Context;
import android.os.Environment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtils{

    private Context context;

    public FileUtils(Context context){
        this.context=context;
    }

    public static String getAppDir(){

        System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        return String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}