package com.crd.gpstracker.util;

import java.util.ArrayList;

import com.crd.gpstracker.activity.Preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Calories {
	private static final String ACTIVITY_TYPE_RUNNING = "Running";
	private static final String ACTIVITY_TYPE_CYCLING = "Cycling";
	private static final String ACTIVITY_TYPE_WALKING = "Walking";
	private static final String ACTIVITY_TYPE_OTHERS = "Others";
	
	private static final String ACTIVITY_TYPE_RUNNING_ZH = "跑步";
	private static final String ACTIVITY_TYPE_CYCLING_ZH = "骑车";
	private static final String ACTIVITY_TYPE_WALKING_ZH = "步行";
	private static final String ACTIVITY_TYPE_OTHERS_ZH = "其它";

	private static final double RUNNING_SLOW_FACTOR = 9.4;
	private static final double RUNNING_NORMAL_FACTOR = 11.3;
	private static final double RUNNING_FAST_FACTOR = 13.2;
	
	private static final double CYCLING_SLOW_FACTOR = 3.0;
	private static final double CYCLING_NORMAL_FACTOR = 6.3;
	private static final double CYCLING_FAST_FACTOR = 9.7;
	
	private static final double WALKING_SLOW_FACTOR = 3.1;
	private static final double WALKING_NORMAL_FACTOR = 3.5;
	private static final double WALKING_FAST_FACTOR = 4.4;
	

	private static final double RUNNING_SLOW_SPEED = 8.7;
	private static final double RUNNING_NORMAL_SPEED = 12.3;
	private static final double RUNNING_FAST_SPEED = 16.0;
	
	private static final double CYCLING_SLOW_SPEED = 8.8;
	private static final double CYCLING_NORMAL_SPEED = 14.8;
	private static final double CYCLING_FAST_SPEED = 20.9;
	
	private static final double WALKING_SLOW_SPEED = 4.0;
	private static final double WALKING_NORMAL_SPEED = 5.0;
	private static final double WALKING_FAST_SPEED = 6.0;
	
	private static final double OTHERS_SPEED = 5.0;
	private static final double OTHERS_FACTOR = 3.0;
	

	protected Context context;
	private static SharedPreferences sharedPreferences;
	
	
	public Calories(Context context) {
		super();
		this.context = context;
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public double getCaloriesFromActivityType(String activityType, double speed, float distance) {
		ArrayList<Double> activityTypeArrayList = new ArrayList<Double>();
		System.out.println("activityType:" + activityType);
		System.out.println("speed:" + speed);
		System.out.println("weight" + sharedPreferences.getString(Preference.WEIGHT, ""));
		double weight = 60.0;
		if(!"".equals(sharedPreferences.getString(Preference.WEIGHT, "")) && sharedPreferences.getString(Preference.WEIGHT, "") != null) {
			weight = Float.parseFloat(sharedPreferences.getString(Preference.WEIGHT, ""));
		}
		
		activityTypeArrayList = getActivityTypeFactorAndSpeed(activityType, speed);
		return weight * activityTypeArrayList.get(0) * distance / activityTypeArrayList.get(1);
	}
	
	private ArrayList<Double> getActivityTypeFactorAndSpeed(String activityType, double speed) {
		ArrayList<Double> arrayList = new ArrayList<Double>();
		if(activityType.equals(ACTIVITY_TYPE_RUNNING) || activityType.equals(ACTIVITY_TYPE_RUNNING_ZH)) {
			if(speed < RUNNING_SLOW_SPEED + (RUNNING_NORMAL_SPEED - RUNNING_SLOW_SPEED) / 2) {
				arrayList.add(RUNNING_SLOW_FACTOR);
				arrayList.add(RUNNING_SLOW_SPEED);
			} else if(speed > RUNNING_FAST_SPEED - (RUNNING_FAST_SPEED - RUNNING_NORMAL_SPEED) / 2) {
				arrayList.add(RUNNING_FAST_FACTOR);
				arrayList.add(RUNNING_FAST_SPEED);
			} else {
				arrayList.add(RUNNING_NORMAL_FACTOR);
				arrayList.add(RUNNING_NORMAL_SPEED);
			}
		} else if(activityType.equals(ACTIVITY_TYPE_CYCLING) || activityType.equals(ACTIVITY_TYPE_CYCLING_ZH)) {
			if(speed < CYCLING_SLOW_SPEED + (CYCLING_NORMAL_SPEED - CYCLING_SLOW_SPEED) / 2) {
				arrayList.add(CYCLING_SLOW_FACTOR);
				arrayList.add(CYCLING_SLOW_SPEED);
			} else if(speed > CYCLING_FAST_SPEED - (CYCLING_FAST_SPEED - CYCLING_NORMAL_SPEED) / 2) {
				arrayList.add(CYCLING_FAST_FACTOR);
				arrayList.add(CYCLING_FAST_SPEED);
			} else {
				arrayList.add(CYCLING_NORMAL_FACTOR);
				arrayList.add(CYCLING_NORMAL_SPEED);
			}
		} else if(activityType.equals(ACTIVITY_TYPE_WALKING) || activityType.equals(ACTIVITY_TYPE_WALKING_ZH)) {
			if(speed < WALKING_SLOW_SPEED + (WALKING_NORMAL_SPEED - WALKING_SLOW_SPEED) / 2) {
				arrayList.add(WALKING_SLOW_FACTOR);
				arrayList.add(WALKING_SLOW_SPEED);
			} else if(speed > WALKING_FAST_SPEED - (WALKING_FAST_SPEED - WALKING_NORMAL_SPEED) / 2) {
				arrayList.add(WALKING_FAST_FACTOR);
				arrayList.add(WALKING_FAST_SPEED);
			} else {
				arrayList.add(WALKING_NORMAL_FACTOR);
				arrayList.add(WALKING_NORMAL_SPEED);
			}
		} else {
			arrayList.add(OTHERS_FACTOR);
			arrayList.add(OTHERS_SPEED);
		}
		return arrayList;

	}
	
	


}
