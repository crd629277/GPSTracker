package com.crd.gpstracker.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.crd.gpstracker.activity.Preference;

public class Environment extends android.os.Environment {

    public static final String TAG = Environment.class.getName();
    private NotificationManager notificationManager;
    private ContentResolver contentResolver;
    private Context context;

    public static final int LED_NOTIFICATION_ID = 0x001;
    public static final int AIRPLANE_MODE_ON = 0x010;
    public static final int AIRPLANE_MODE_OFF = 0x000;

    public static final String SQLITE_DATABASE_FILENAME_EXT = ".sqlite";

    public Environment(Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getExternalStoragePath() {
        if (isExternalStoragePresent()) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }


    public static File getStorageDirectory(Date date) {
        String storageDirectory = Environment.getExternalStoragePath()
            + File.separator + "gpstracker" + File.separator;

        storageDirectory += File.separator
            + new SimpleDateFormat("yyyyMM").format(date);

        return new File(storageDirectory);
    }

    public static File getStorageDirectory() {
        return getStorageDirectory(new Date());
    }

    public static File getDatabaseFile(String recordBy) {
        File storageDirectory = getStorageDirectory();
        String databaseFileName = System.currentTimeMillis() + SQLITE_DATABASE_FILENAME_EXT;

        // If record by day, output the database filename like "20111203.sqlite" etc.
        if (recordBy.equals(Preference.RECORD_BY_DAY)) {
            databaseFileName = (new SimpleDateFormat("yyyyMMdd").format(new Date())) + ".sqlite";
        }

        File databaseFile = new File(storageDirectory.getAbsoluteFile() + File.separator + databaseFileName);
        Log.e("", databaseFile.getAbsolutePath());

        return databaseFile;
    }

    public void turnOnLED() {
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 1000;
        notif.ledOffMS = 1500;
        notificationManager.notify(LED_NOTIFICATION_ID, notif);
    }

    public void turnOffLED() {
        notificationManager.cancel(LED_NOTIFICATION_ID);
    }


    public void setAirPlaneMode(int mode) {
        switch (mode) {
            case AIRPLANE_MODE_OFF:
                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_OFF);
                break;

            case AIRPLANE_MODE_ON:
                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_ON);
                break;

            default:
                return;
        }

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.sendBroadcast(intent);
    }

    public int getCurrentAirPlaneMode() {
        try {
            return Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }
}
