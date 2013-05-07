package com.crd.gpstracker.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.ActivityTypeUtil;
import com.crd.gpstracker.util.Helper;
import com.crd.gpstracker.util.Helper.Logger;

public class ArchiveMetaFragment extends Fragment {
	public ArchiveMeta meta;
	public ActivityTypeUtil calories;
	private Context context;
	private View layoutView;
	private TextView mDistance;
	private TextView mAvgSpeed;
	private TextView mMaxSpeed;
	private TextView mRecords;
	private TextView mActivityType;
	private TextView mCalories;
	private Helper helper;
	
	private String formatter;

	public ArchiveMetaFragment(Context context, ArchiveMeta meta) {
		this.meta = meta;
		this.context = context;
		this.formatter = context.getString(R.string.records_formatter);
		calories = new ActivityTypeUtil(context);
		helper = new Helper(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutView = inflater.inflate(R.layout.archive_meta_items, container,
				false);
		mDistance = (TextView) layoutView.findViewById(R.id.item_distance);
		mAvgSpeed = (TextView) layoutView.findViewById(R.id.item_avg_speed);
		mMaxSpeed = (TextView) layoutView.findViewById(R.id.item_max_speed);
		mRecords = (TextView) layoutView.findViewById(R.id.item_records);
		mActivityType = (TextView) layoutView.findViewById(R.id.item_activity_type);
		mCalories = (TextView) layoutView.findViewById(R.id.item_calories);
		setRetainInstance(true);
		return layoutView;
	}

	@Override
	public void onStart() {
		super.onStart();
		update();
	}

	public void update() {
		String[] activityType = getResources().getStringArray(R.array.activityType);
		try {
			mActivityType.setText(activityType[meta.getActivityType()]);
			mDistance.setText(String.format(formatter, meta.getDistance() / ArchiveMeta.TO_KILOMETRE));
//			mMaxSpeed.setText(String.format(formatter, meta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
//			mAvgSpeed.setText(String.format(formatter, meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
			mMaxSpeed.setText(String.format(formatter, helper.changeSpeedToMinPerHour(meta.getMaxSpeed())));
			mAvgSpeed.setText(String.format(formatter, helper.changeSpeedToMinPerHour(meta.getAverageSpeed())));
			mRecords.setText(String.valueOf(meta.getCount()));
			if(meta.getCalories() > 0) {
				mCalories.setText(String.valueOf(meta.getCalories()));
			} else {
				mCalories.setText(String.format(formatter, calories.getCaloriesFromActivityType(meta.getActivityType(), 
						meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT, meta.getDistance() / ArchiveMeta.TO_KILOMETRE)));
			}
			
		} catch (Exception e) {
			Logger.e(e.getMessage());
		}
	}

	public void update(ArchiveMeta meta) {
		this.meta = meta;
		this.update(meta);
	}

}
