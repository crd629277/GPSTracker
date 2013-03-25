package com.crd.gpstracker.service;

import java.math.BigDecimal;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.util.Logger;

public class Listener implements LocationListener {
    private Archive archive;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;
    private final static int ACCURACY = 4;

    public Listener(Archive archive) {
        this.archive = archive;
    }

    private boolean filter(Location location) {
        BigDecimal longitude = (new BigDecimal(location.getLongitude())).setScale(
            ACCURACY, BigDecimal.ROUND_HALF_UP);

        BigDecimal latitude = (new BigDecimal(location.getLatitude())).setScale(
            ACCURACY, BigDecimal.ROUND_HALF_UP);

        if (latitude.equals(lastLatitude) && longitude.equals(lastLongitude)) {
            return false;
        }

        lastLatitude = latitude;
        lastLongitude = longitude;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (filter(location) && archive.add(location)) {
            Logger.i(String.format(
                "Location(%f, %f) has been saved into database.", lastLatitude, lastLongitude
            ));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:

                break;
            case LocationProvider.OUT_OF_SERVICE:

                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}


