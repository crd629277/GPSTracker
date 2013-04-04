package com.crd.gpstracker.service;

import java.math.BigDecimal;
import java.util.HashMap;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.Helper.Logger;

/**
 * 绑定 LocationListener 回调并记录到数据库
 * 
 * @author Chen
 *
 */
public class Listener implements LocationListener {
	private final static int ACCURACY = 3;
//	private final static int CACHE_SIZE = 10;
	
    private Archive archive;
    private ArchiveMeta meta = null;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;
//    private HashMap<Long, Location> locationCache;

    public Listener(Archive archive) {
        this.archive = archive;
//        this.locationCache = new HashMap<Long, Location>();
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
            this.meta = archive.getMeta();
            Logger.i(String.format(
                "Location(%f, %f) has been saved into database.", lastLatitude, lastLongitude
            ));

            // 另外开个线程处理，避免线程锁住
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (meta != null) {
                        meta.setRawDistance();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
            	Logger.i("Location provider is available.");
                break;
            case LocationProvider.OUT_OF_SERVICE:
            	Logger.w("Location provider is out of service.");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
            	Logger.w("Location provider is temporarily unavailable.");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
    	Logger.i("Location provider is enabled.");
    }

    @Override
    public void onProviderDisabled(String s) {
    	Logger.w("Location provider is disabled.");
    }
}


