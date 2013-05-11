package com.crd.gpstracker.service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.Preference;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.ActivityTypeUtil;
import com.crd.gpstracker.util.Helper;
import com.crd.gpstracker.util.Helper.Logger;
import com.crd.gpstracker.util.Notifier;
import com.umeng.analytics.MobclickAgent;

interface Binder {
    public static final int STATUS_RECORDING = 0x0000;
    public static final int STATUS_STOPPED = 0x1111;

    public void startRecord(int activityTypePosition);

    public void stopRecord();

    public int getStatus();

    public ArchiveMeta getMeta();

    public Archive getArchive();

    public Location getLastRecord();
    
    public void closeDB();
}

public class Recorder extends Service {
    protected Recorder.ServiceBinder serviceBinder = null;
    private SharedPreferences sharedPreferences;
    private Archive archive;

    private Listener listener;
    private StatusListener statusListener;
    private LocationManager locationManager = null;

    private ArchiveNameHelper nameHelper;
    private String archivName;
    private Helper helper;
    private Context context;
    private Notifier notifier;
    private ActivityTypeUtil calories;
    
    private static final String RECORDER_SERVER_ID = "Tracker Service";
    private static final String PREF_STATUS_FLAG = "Tracker Service Status";
    private TimerTask notifierTask;
//    protected TimerTask costTimeTask;
    private Timer timer = null;
//    private Timer costTimer = null;
    
    

    public class ServiceBinder extends android.os.Binder implements Binder {
    	
//    	protected int mCostTime = 0;
    	
        ServiceBinder() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            archive = new Archive(getApplicationContext());
            listener = new Listener(archive, this);
            statusListener = new StatusListener();
            calories = new ActivityTypeUtil(context);
        }

        @Override
        public void startRecord(int activityTypePosition) {
            if (getStatus() != ServiceBinder.STATUS_RECORDING) {
            	
            	// 设置启动时更新配置
                notifier = new Notifier(context);
            	
            	// 如果没有外置存储卡
                if (!nameHelper.isExternalStoragePresent()) {
                    helper.showLongToast(getString(R.string.external_storage_not_present));
                    return;
                }
            	
                // 从配置文件获取距离和精度选项
                long minTime = Long.parseLong(sharedPreferences.getString(Preference.GPS_MINTIME,
                    Preference.DEFAULT_GPS_MINTIME));
                float minDistance = Float.parseFloat(sharedPreferences.getString(Preference.GPS_MINDISTANCE,
                    Preference.DEFAULT_GPS_MINDISTANCE));

                // 判定是否上次为异常退出
                boolean hasResumeName = nameHelper.hasResumeName();
                if (hasResumeName) {
                    archivName = nameHelper.getResumeName();
                    helper.showLongToast(
                        String.format(
                            getString(R.string.use_resume_archive_file, archivName)
                        ));
                } else {
                    archivName = nameHelper.getNewName();
                }

                try {
                    archive.open(archivName, Archive.MODE_READ_WRITE);
//                    nameHelper.setLastOpenedName(archivName);

                    // 设置开始时间，如果是恢复文件，则就不设置
                    if(!hasResumeName) {
                    	getMeta().setStartTime(new Date());
                    }
                    
                    if(activityTypePosition != -1) {
                    	getMeta().setActivityType(activityTypePosition);
                    }

                    
                    // 绑定 GPS 回调
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDistance, listener);
                    locationManager.addGpsStatusListener(statusListener);

                    // 标记打开的文件，方便崩溃时恢复
                    nameHelper.setLastOpenedName(archivName);
                } catch (SQLiteException e) {
                    Logger.e(e.getMessage());
                }
                
              //开个线程计算当前运动的时间
//                costTimeTask = new TimerTask() {
//					@Override
//					public void run() {
//						switch (serviceBinder.getStatus()) {
//						case ServiceBinder.STATUS_RECORDING:
//							mCostTime++;
//							break;
//
//						case ServiceBinder.STATUS_STOPPED:
//							mCostTime = 0;
//							break;
//						}
//						
//					}
//				};
//				
//				timer = new Timer();
//                timer.schedule(costTimeTask, 0, 1000);
                

                // 另开个线程展示通知信息
                notifierTask = new TimerTask() {
                    @Override
                    public void run() {
                    	switch (serviceBinder.getStatus()) {
						case ServiceBinder.STATUS_RECORDING:
							ArchiveMeta meta = getMeta();
//							float distance = meta.getDistance() / ArchiveMeta.TO_KILOMETRE;
//	                        double avgSpeed = meta.getAverageSpeed() * ArchiveMeta.TO_KILOMETRE;
//	                        double maxSpeed = meta.getMaxSpeed() * ArchiveMeta.TO_KILOMETRE;
	                        
//	                        notifier.setStatusString(String.format(getString(R.string.status_format), distance, avgSpeed, maxSpeed));
//	                        notifier.setCostTimeString(meta.getCostTimeStringByNow());
	                        
	                        notifier.publish();
	                        
							break;

						case ServiceBinder.STATUS_STOPPED:
							notifier.cancel();
							break;
						}
                        
                    }
                };
                timer = new Timer();
                timer.schedule(notifierTask, 0, 5000);
                
                // Set status from shared preferences, default is stopped.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_RECORDING);
                editor.commit();
                
                MobclickAgent.onEvent(context, RECORDER_SERVER_ID);
            }
        }
        
//        public String getCostTime() {
//        	return getMeta().converTimeToString(mCostTime);
//        }
        
        
        public GpsStatus getGpsStatus() {
        	return locationManager.getGpsStatus(null);
        }
        
        public void resetStatus() {
        	SharedPreferences.Editor editor = sharedPreferences.edit();
        	editor.putInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
        	editor.commit();
        }

        @Override
        public void stopRecord() {
            if (getStatus() == ServiceBinder.STATUS_RECORDING) {
                locationManager.removeUpdates(listener);
                locationManager.removeGpsStatusListener(statusListener);

                ArchiveMeta meta = getMeta();
                long totalCount = meta.getCount();
                if (totalCount < 2) {
                    (new File(archivName)).delete();
                    helper.showLongToast(getString(R.string.not_record_anything));
                } else {
                    // 设置结束记录时间和所花时间
                	meta.setEndTime(new Date());
//                	meta.setCostTime(mCostTime);
                	double totalCalorie = calories.getCaloriesFromActivityType(meta.getActivityType(), 
        					meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT, meta.getDistance() / ArchiveMeta.TO_KILOMETRE);
                	
                	meta.setCalories(totalCalorie);
//                    helper.showLongToast(String.format(getString(R.string.result_report), String.valueOf(totalCount)));
                }

                // 清除操作
//                archive.close();
                notifier.cancel();
                if(timer != null) {
                	timer.cancel();
                	timer = null;
                }
                
                nameHelper.clearLastOpenedName();

                resetStatus();
                MobclickAgent.onEvent(context, RECORDER_SERVER_ID);
            }
        }

        @Override
        public int getStatus() {
            return sharedPreferences.getInt(PREF_STATUS_FLAG, ServiceBinder.STATUS_STOPPED);
        }

        @Override
        public ArchiveMeta getMeta() {
            return archive.getMeta();
        }

        @Override
        public Archive getArchive() {
            return archive;
        }

        @Override
        public Location getLastRecord() {
            return archive.getLastRecord();
        }

		@Override
		public void closeDB() {
			archive.close();
		}
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.nameHelper = new ArchiveNameHelper(context);
        this.helper = new Helper(context);
        if(this.serviceBinder == null) {
        	this.serviceBinder = new ServiceBinder();
        }

//        boolean autoStart = sharedPreferences.getBoolean(Preference.AUTO_START, false);
        boolean alreadyStarted = (serviceBinder.getStatus() == ServiceBinder.STATUS_RECORDING);
        
        if (alreadyStarted) {
        	if(alreadyStarted) {
        		serviceBinder.resetStatus();
        	}
            serviceBinder.startRecord(-1);
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }
    
    @Override
    public void onDestroy() {
    	serviceBinder.stopRecord();
    	super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}

