package com.crd.gpstracker.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.crd.gpstracker.RecordService;
import com.crd.gpstracker.dao.GPSDatabase;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {
    protected GPSDatabase gpsDatabase;
    protected SharedPreferences sharedPreferences;

    protected RecordService.ServiceBinder serviceBinder;
    public ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (RecordService.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        MobclickAgent.onError(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onDestroy() {
        if (serviceBinder != null) {
            unbindService(serviceConnection);
            serviceBinder = null;
        }

        super.onDestroy();
    }
}
