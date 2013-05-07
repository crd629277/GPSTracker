package com.crd.gpstracker.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.crd.gpstracker.R;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.Helper;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class SpeedChartsFragment extends Fragment{
	private Context context;
    private Archive archive;
    private ArrayList<Location> locations;
    private BarGraphView barGraphView;
    private static int HORIZONTAL_LABELS_ITEM_SIZE = 9;
    private ArrayList<GraphViewData> speedSeries;
    private SimpleDateFormat dateFormatter;
    private Helper helper;

	
	public SpeedChartsFragment(Context context, Archive archive) {
		this.context = context;
		this.archive = archive;
		
		helper = new Helper(context);
		this.locations = archive.fetchAll();
		speedSeries = new ArrayList<GraphViewData>();
		dateFormatter = new SimpleDateFormat(context.getString(R.string.sort_time_format), Locale.getDefault());
		
		barGraphView = new BarGraphView(context, "");
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setGraphData();
		return barGraphView;
	} 
	
	@Override
	public void onStart() {
		super.onStart();
	}


	private void setGraphData() {
		//label
		barGraphView.setHorizontalLabels(getHorizontalLabels());
		
		//data	
		barGraphView.addSeries(new GraphViewSeries(getSeriesData()));
		
	}
	
	
	private String[] getHorizontalLabels() {
		int size = locations.size();
		ArrayList<String> labels = new ArrayList<String>();
		
		for(int i=0; i< size; i+=(size/HORIZONTAL_LABELS_ITEM_SIZE)) {
			Location location = locations.get(i);
			if (location != null) {
				labels.add(dateFormatter.format(location.getTime()));
			}
		}
		
		return labels.toArray(new String[labels.size()]);
	}
	
	
	private GraphViewData[] getSeriesData() {
		speedSeries.clear();
		
		Iterator<Location> locationIterator = locations.iterator();
		while(locationIterator.hasNext()) {
			Location location = locationIterator.next();
//			GraphViewData graphViewData = new GraphViewData(location.getTime(), helper.changeSpeedToMinPerHour(location.getSpeed()));
			GraphViewData graphViewData = new GraphViewData(location.getTime(), location.getSpeed() * ArchiveMeta.KM_PER_HOUR_CNT);
			speedSeries.add(graphViewData);
		}
		
		return speedSeries.toArray(new GraphViewData[speedSeries.size()]);
	}
	
	

	
	

}
