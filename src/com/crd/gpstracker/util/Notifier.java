package com.crd.gpstracker.util;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.Tracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Notifier extends Notification{
    private NotificationManager notificationManager;
    public static final int LED_NOTIFICATION_ID = 0x001;
    public static final int NOTIFICATION_ID = 0x0001;

    protected Context context;
    private Intent intent;

    public Notifier(Context context) {
    	super(R.drawable.ic_notification, context.getString(R.string.running), System.currentTimeMillis());
    	
    	this.flags |= Notification.FLAG_ONGOING_EVENT;
    	this.flags |= Notification.DEFAULT_LIGHTS;
    	this.context = context;
    	
    	this.intent = new Intent(context, Tracker.class);
    	this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	this.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	this.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
    	this.contentView = new RemoteViews(context.getPackageName(), R.layout.notifier);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    
    public void setStatusString(String statusString) {
		contentView.setTextViewText(R.id.status, statusString);
	}
    
    public void setCostTimeString(String costTimeString) {
        contentView.setTextViewText(R.id.status_cost_time, costTimeString);
    }
    
    public void setNumber(int number) {
		this.number = number;
	}
    
    public void publish() {
    	notificationManager.notify(NOTIFICATION_ID, this);
    	
    }
    
    
    public void cancel() {
    	notificationManager.cancelAll();
    }

}
