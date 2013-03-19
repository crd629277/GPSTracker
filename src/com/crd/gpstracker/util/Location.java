package com.crd.gpstracker.util;


import java.text.DecimalFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.crd.gpstracker.dao.Database;

public class Location implements android.location.LocationListener {

    private final String TAG = Location.class.getName();

    private static SQLiteDatabase db;
    private Context context;

    public Location(Context context) {
        this.context = context;
        db = new Database(context).getWritableDatabase();
    }

    private String latitude, longitude;

    @Override
    public void onLocationChanged(android.location.Location loc) {
        ContentValues values = new ContentValues();

        DecimalFormat formatter = new DecimalFormat("####.###");
        String tmpLongitude = formatter.format(loc.getLongitude());
        String tmpLatitude = formatter.format(loc.getLatitude());

        // @todo need new fliter for this feature
        if (tmpLatitude.equals(latitude) && tmpLongitude.equals(longitude)) {
            Log.v(TAG, String.format("The same latitude %f and longitude %f, ignore this.",
                loc.getLatitude(), loc.getLongitude()));
            return;
        }
        latitude = tmpLatitude;
        longitude = tmpLongitude;

        values.put("latitude", loc.getLatitude());
        values.put("longitude", loc.getLongitude());
        values.put("speed", loc.getSpeed());
        values.put("bearing", loc.getBearing());
        values.put("altitude", loc.getAltitude());
        values.put("accuracy", loc.getAccuracy());
        values.put("time", loc.getTime());

        try {
            db.insert("location", null, values);
            Log.v(TAG, String.format("gps record which latitude is %.3f and is longitude %.3f has saved.",
                loc.getLatitude(), loc.getLongitude()));
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "GPS is enabled, reopen database.");
        db = new Database(context).getWritableDatabase();
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.w(TAG, "GPS is disabled");
        db.close();
    }
}
