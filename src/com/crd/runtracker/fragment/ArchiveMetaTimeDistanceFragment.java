package com.crd.runtracker.fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crd.runtracker.R;
import com.crd.runtracker.dao.ArchiveMeta;

public class ArchiveMetaTimeDistanceFragment extends Fragment {
    private ArchiveMeta meta;
    private View metaLayout;
    private SimpleDateFormat dateFormat;
    private String formatter;
    private TextView mCostTime;
    private TextView mDistance;
    private Context context;

    public ArchiveMetaTimeDistanceFragment(Context context, ArchiveMeta meta) {
        this.meta = meta;
        this.context = context;
        this.formatter = context.getString(R.string.records_formatter);
        this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        metaLayout = inflater.inflate(R.layout.archive_meta_time_distance, container, false);
        mCostTime = (TextView) metaLayout.findViewById(R.id.item_cost_time);
        mDistance = (TextView) metaLayout.findViewById(R.id.item_distance);
		setRetainInstance(true);
        return metaLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
    }

    protected void updateView() {
    	String totalTime = meta.getRawCostTimeString();
    	mCostTime.setText(totalTime.length() > 0 ? totalTime : getString(R.string.not_available));
    	mDistance.setText(String.format(formatter, meta.getDistance() / ArchiveMeta.TO_KILOMETRE));
        
    }
}
