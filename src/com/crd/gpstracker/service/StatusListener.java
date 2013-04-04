package com.crd.gpstracker.service;

import android.location.GpsStatus;

import com.crd.gpstracker.util.Helper.Logger;

public class StatusListener implements GpsStatus.Listener{

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
            	Logger.i("GPS event is started.");
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
            	Logger.i("GPS event is first fixed.");
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
            	Logger.i("GPS EVENT SATELLITE STATUS.");
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
            	Logger.i("GPS event is stopped.");
                break;
        }
    }

}
