package com.crd.gpstracker.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.ActivityTypeUtil;
import com.crd.gpstracker.util.Helper;
import com.crd.gpstracker.util.Helper.Logger;

public class ArchiveMetaDetailFragment extends Fragment {
	public ArchiveMeta meta;
	public ActivityTypeUtil calories;
	private Context context;
	private View layoutView;
	private TextView mAvgSpeed;
	private TextView mStartTime;
	private TextView mRecords;
	private TextView mActivityType;
	private ImageView mActivityTypeImage;
	private TextView mCalories;
	private Helper helper;
	private ActivityTypeUtil activityTypeUtil;
	
	private String formatter;
	private SimpleDateFormat dateFormat;

	public ArchiveMetaDetailFragment(Context context, ArchiveMeta meta) {
		this.meta = meta;
		this.context = context;
		this.formatter = context.getString(R.string.records_formatter);
		this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
		calories = new ActivityTypeUtil(context);
		activityTypeUtil = new ActivityTypeUtil(context);
		helper = new Helper(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutView = inflater.inflate(R.layout.archive_meta_items_detail, container,
				false);
		mStartTime = (TextView) layoutView.findViewById(R.id.item_start_time);
		mAvgSpeed = (TextView) layoutView.findViewById(R.id.item_avg_speed);
		mActivityTypeImage = (ImageView) layoutView.findViewById(R.id.activity_type_image);
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
		Date startTime = meta.getStartTime();
		try {
			mActivityType.setText(activityType[meta.getActivityType()]);
			mActivityTypeImage.setImageBitmap(activityTypeUtil.getImageFromActivityType(meta.getActivityType()));
			mStartTime.setText(startTime != null ? dateFormat.format(startTime) : getString(R.string.not_available));
			mAvgSpeed.setText(String.format(formatter, helper.changeSpeedToMinPerHour(meta.getAverageSpeed())));
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
