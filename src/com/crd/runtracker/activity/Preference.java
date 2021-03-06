package com.crd.runtracker.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

import com.crd.runtracker.R;
import com.markupartist.android.widget.ActionBar;

public class Preference extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String FULL_NAME = "fullName";
	public static final String GENDER = "gender";
	public static final String HEIGHT = "height";
	public static final String WEIGHT = "weight";
	
    public static final String USER_ORIENTATION = "orientation";
    public static final String GPS_MINTIME = "gpsMinTime";
    public static final String GPS_MINDISTANCE = "gpsMinDistance";

    public static final String DEFAULT_GPS_MINTIME = "2000";
    public static final String DEFAULT_GPS_MINDISTANCE = "10";
    private SharedPreferences preferenceManager;
    public static final String DEFAULT_USER_ORIENTATION = "portrait";
    
    private EditTextPreference mFullNameEditTextPreference;
    private ListPreference mGenderListPreference;
    private EditTextPreference mHeightEditTextPreference; 
    private EditTextPreference mWeightEditTextPreference; 
    private ListPreference mGPSTimeInterval;
    private ListPreference mGPSDistanceInterval;
    private ActionBar actionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        setContentView(R.layout.preference_screen);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceManager.registerOnSharedPreferenceChangeListener(this);
        
        mFullNameEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(FULL_NAME);
        mGenderListPreference = (ListPreference) getPreferenceScreen().findPreference(GENDER);
        mHeightEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(HEIGHT);
        mWeightEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(WEIGHT);
        
        mGPSTimeInterval = (ListPreference) getPreferenceScreen().findPreference(GPS_MINTIME);
        mGPSDistanceInterval = (ListPreference) getPreferenceScreen().findPreference(GPS_MINDISTANCE);
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_back;
            }

            @Override
            public void performAction(View view) {
                finish();
            }
        });
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mFullNameEditTextPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(FULL_NAME, ""));
    	mGenderListPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(GENDER, ""));
    	mHeightEditTextPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(HEIGHT, ""));
    	mWeightEditTextPreference.setSummary(getPreferenceScreen().getSharedPreferences().getString(WEIGHT, ""));
    	mGPSTimeInterval.setSummary(getPreferenceScreen().getSharedPreferences().getString(GPS_MINTIME, ""));
    	mGPSDistanceInterval.setSummary(getPreferenceScreen().getSharedPreferences().getString(GPS_MINDISTANCE, ""));
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
        	mGenderListPreference.setSummary(sharedPreferences.getString(key, ""));
        } else if(key.equals(WEIGHT)) {
        	if(sharedPreferences.getString(key, "") != null || !"".equals(sharedPreferences.getString(key, ""))) {
        		String weightFormatter = getString(R.string.weight_formatter);
            	String weightSummary = String.format(weightFormatter, Float.parseFloat(sharedPreferences.getString(key, "")));
            	mWeightEditTextPreference.setSummary(weightSummary  + " kg");
            	mWeightEditTextPreference.setText(weightSummary);
        	} else {
        		mWeightEditTextPreference.setText("60.00");
        		mWeightEditTextPreference.setSummary("60.00 kg");
        	}
        } else if(key.equals(HEIGHT)) {
        	if(sharedPreferences.getString(key, "") != null || !"".equals(sharedPreferences.getString(key, ""))) {
        		String heightFormatter = getString(R.string.height_formatter);
            	String heightSummary = String.format(heightFormatter, Float.parseFloat(sharedPreferences.getString(key, "")));
            	mHeightEditTextPreference.setSummary(heightSummary  + " cm");
            	mHeightEditTextPreference.setText(heightSummary);
        	} else {
        		mHeightEditTextPreference.setText("170.0");
        		mHeightEditTextPreference.setSummary("170.0 cm");
        	}
        } else if(key.equals(GPS_MINTIME)) {
        	mGPSTimeInterval.setSummary(sharedPreferences.getString(key, ""));
        } else if(key.equals(GPS_MINDISTANCE)){
        	mGPSDistanceInterval.setSummary(sharedPreferences.getString(key, ""));
		}
    }
}