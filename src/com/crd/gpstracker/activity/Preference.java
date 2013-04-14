package com.crd.gpstracker.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.crd.gpstracker.R;

public class Preference extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String FULL_NAME = "fullName";
	public static final String GENDER = "gender";
	public static final String WEIGHT = "weight";
	
    public static final String USER_ORIENTATION = "orientation";
    public static final String AUTO_START = "autoStart";
    public static final String RECORD_BY = "recordBy";
    public static final String LIGHTNING_LED = "lightLed";
    public static final String GPS_MINTIME = "gpsMinTime";
    public static final String GPS_MINDISTANCE = "gpsMinDistance";
    public static final String AUTO_CLEAN = "autoClean";
    public static final String SWITCH_AIRPLANE_MODE = "switchAirplaneMode";

    public static final String RECORD_BY_DAY = "RECORD_BY_DAY";
    public static final String RECORD_BY_TIMES = "RECORD_BY_TIMES";

    public static final String DEFAULT_GPS_MINTIME = "2000";
    public static final String DEFAULT_GPS_MINDISTANCE = "10";
    private SharedPreferences preferenceManager;
    public static final String DEFAULT_USER_ORIENTATION = "portrait";
    
    private EditTextPreference mFullNameEditTextPreference;
    private ListPreference mGenderListPreference;
    private EditTextPreference mWeightEditTextPreference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceManager.registerOnSharedPreferenceChangeListener(this);
        
        mFullNameEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(FULL_NAME);
        mGenderListPreference = (ListPreference) getPreferenceScreen().findPreference(GENDER);
        mWeightEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(WEIGHT);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mFullNameEditTextPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(FULL_NAME, ""));
    	mGenderListPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(GENDER, ""));
    	mWeightEditTextPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(WEIGHT, ""));
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Boolean autoClean = sharedPreferences.getBoolean(AUTO_CLEAN, true);
        
        if(key.equals(FULL_NAME)) {
        	mFullNameEditTextPreference.setSummary(sharedPreferences.getString(key, ""));
        } else if(key.equals(GENDER)) {
        	mGenderListPreference.setSummary(sharedPreferences.getString(key, "Male"));
        } else if(key.equals(WEIGHT)) {
        	if(sharedPreferences.getString(key, "") != null || !"".equals(sharedPreferences.getString(key, ""))) {
        		String weightFormatter = getString(R.string.weight_formatter);
            	String weightSummary = String.format(weightFormatter, Float.parseFloat(sharedPreferences.getString(key, "")));
            	mWeightEditTextPreference.setSummary(weightSummary  + " kg");
            	mWeightEditTextPreference.setText(weightSummary);
        	} else {
        		mWeightEditTextPreference.setText("60.00");
        		mWeightEditTextPreference.setSummary("0.00 kg");
        	}
        	
        }
        
//        if (key.equals(AUTO_CLEAN) && !autoClean) {
//
////            SharedPreferences.Editor editor = sharedPreferences.edit();
////            editor.putBoolean(AUTO_CLEAN, true);
////            editor.commit();
//
//        }
    }
}