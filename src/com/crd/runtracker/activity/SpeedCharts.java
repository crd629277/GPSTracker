package com.crd.runtracker.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.crd.runtracker.R;
import com.crd.runtracker.activity.base.Activity;
import com.crd.runtracker.dao.Archive;
import com.crd.runtracker.dao.ArchiveMeta;
import com.crd.runtracker.fragment.SpeedChartsFragment;
import com.markupartist.android.widget.ActionBar;

public class SpeedCharts extends Activity {
	private String archiveFileName;
	private Archive archive;
	private String description;
	private LinearLayout chartsView;
	private ArchiveMeta archiveMeta;
	private SpeedChartsFragment speedChartsFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_charts);
		
		archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
		archive = new Archive(context, archiveFileName, Archive.MODE_READ_ONLY);
		archiveMeta = archive.getMeta();
		
		description = archive.getMeta().getDescription();
		if(description.length() <=0 ) {
			description = getString(R.string.no_description);
		}
		
		speedChartsFragment = new SpeedChartsFragment(context, archive);
		chartsView = (LinearLayout) findViewById(R.id.charts);
	} 
	
	
	@Override
    public void onStart() {
        super.onStart();
        actionBar.setTitle(description);
        actionBar.removeAllActions();
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
        addFragment(R.id.charts, speedChartsFragment);
    }


    private Bitmap getChartsBitmap() {
        chartsView.setDrawingCacheEnabled(true);
        chartsView.buildDrawingCache();
        chartsView.destroyDrawingCache();
        return Bitmap.createBitmap(chartsView.getDrawingCache());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        archive.close();
    }
	
	
}
