package com.crd.gpstracker.util;

import android.util.Log;

public class Logger {
    protected static final String TAG = "GPSTracker";

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}
