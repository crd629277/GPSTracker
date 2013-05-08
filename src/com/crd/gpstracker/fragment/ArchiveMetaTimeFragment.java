package com.crd.gpstracker.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.dao.ArchiveMeta;

public class ArchiveMetaTimeFragment extends Fragment {
    private ArchiveMeta meta;
    private View metaLayout;
    private SimpleDateFormat dateFormat;
    private TextView mStartTime;
    private TextView mTotalTime;
    private Context context;

    public ArchiveMetaTimeFragment(Context context, ArchiveMeta meta) {
        this.meta = meta;
        this.context = context;
        this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        metaLayout = inflater.inflate(R.layout.archive_meta_time, container, false);
        mStartTime = (TextView) metaLayout.findViewById(R.id.meta_start_time);
        mTotalTime = (TextView) metaLayout.findViewById(R.id.total_time);
		setRetainInstance(true);
        return metaLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
    }

    protected void updateView() {
    	Date startTime = meta.getStartTime();
    	Date endTime = meta.getEndTime();
    	String totalTime = meta.getRawCostTimeString();
        mStartTime.setText(startTime != null ? dateFormat.format(startTime) : getString(R.string.not_available));
        mTotalTime.setText(totalTime.length() > 0 ? totalTime : getString(R.string.not_available));
        
    }
}
