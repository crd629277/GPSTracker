package com.crd.gpstracker.activity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.Activity;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.fragment.SpeedChartsFragment;
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
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_share;
            }

            @Override
            public void performAction(View view) {
                shareToSina();
            }
        });
        
        addFragment(R.id.charts, speedChartsFragment);
    }
	
	
	public void shareToSina() {
        byte[] bitmap = helper.convertBitmapToByteArray(getChartsBitmap());

        String recordsFormatter = getString(R.string.records_formatter);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.time_format), Locale.getDefault());

        // Build string for share by microblog etc.
        String message = String.format(getString(R.string.share_report_formatter),
            archiveMeta.getDescription().length() > 0 ? "(" + archiveMeta.getDescription() + ")" : "",
            String.format(recordsFormatter, archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE),
            dateFormatter.format(archiveMeta.getStartTime()),
            dateFormatter.format(archiveMeta.getEndTime()),
            archiveMeta.getRawCostTimeString(),
            String.format(recordsFormatter, archiveMeta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT),
            String.format(recordsFormatter, archiveMeta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT)
        );

        helper.shareToSina(context, message, bitmap);
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
