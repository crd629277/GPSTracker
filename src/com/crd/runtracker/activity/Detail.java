package com.crd.runtracker.activity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;

import com.crd.runtracker.R;
import com.crd.runtracker.activity.base.Activity;
import com.crd.runtracker.activity.maps.BaiduMap;
import com.crd.runtracker.activity.maps.BaiduMapDetail;
import com.crd.runtracker.dao.Archive;
import com.crd.runtracker.dao.ArchiveMeta;
import com.crd.runtracker.fragment.ArchiveMetaDetailFragment;
import com.crd.runtracker.fragment.ArchiveMetaTimeDistanceFragment;
import com.markupartist.android.widget.ActionBar;

public class Detail extends Activity implements View.OnTouchListener, View.OnClickListener{
    private String archiveFileName;

    private Archive archive;
    private ArchiveMeta archiveMeta;

    private ArchiveMetaDetailFragment archiveMetaDetailFragment;
    private ArchiveMetaTimeDistanceFragment archiveMetaTimeDistanceFragment;

//    private TextView mDescription;
    private LocalActivityManager localActivityManager;
    private TabHost mTabHost;
    private View mMapMask;
    public static final String INSIDE_TABHOST = "inside_tabhost";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        
        localActivityManager = new LocalActivityManager(this, false);
        localActivityManager.dispatchCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        mMapMask = findViewById(R.id.map_mask);
//        mDescription = (TextView) findViewById(R.id.item_description);
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        archiveMetaDetailFragment = new ArchiveMetaDetailFragment(context, archiveMeta);
        archiveMetaTimeDistanceFragment = new ArchiveMetaTimeDistanceFragment(context, archiveMeta);

        addArchiveMetaTimeFragment();
        addArchiveMetaFragment();
        
//        mDescription.setOnClickListener(this);
        mMapMask.setOnTouchListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        String description = archiveMeta.getDescription();
        if(description.length() > 0) {
        	actionBar.setTitle(description);
        } else {
        	actionBar.setTitle(getString(R.string.no_description));
        }
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
        
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_speedchar;
            }

            @Override
            public void performAction(View view) {
            	Intent intent = new Intent(context, SpeedCharts.class);
            	intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
            	startActivity(intent);
            }
        });
        
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
    }
    
    
    public void shareToSina() {
    	String[] activityType = getResources().getStringArray(R.array.activityType);
    	byte[] bitmap = helper.convertBitmapToByteArray(getRouteBitmap());
    	String recordsFormatter = getString(R.string.records_formatter);
    	SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.time_format), Locale.CHINA);
    	
    	// Build string for share by microblog etc.
        String message = String.format(getString(R.string.share_report_formatter),
        	activityType[archiveMeta.getActivityType()],
            archiveMeta.getDescription().length() > 0 ? ", " + archiveMeta.getDescription() : "",
            String.format(recordsFormatter, archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE),
            dateFormatter.format(archiveMeta.getStartTime()),
            dateFormatter.format(archiveMeta.getEndTime()),
            archiveMeta.getRawCostTimeString(),
            String.format(recordsFormatter, archiveMeta.getCalories()),
            String.format(recordsFormatter, helper.changeSpeedToMinPerHour(archiveMeta.getAverageSpeed()))
        );
        helper.shareToSina(context, message, bitmap);
    	
    }
    
    
    private void confirmDelete() {
        helper.showConfirmDialog(
            getString(R.string.delete),
            String.format(getString(R.string.sure_to_del), archiveMeta.getName()),
            new Runnable() {
                @Override
                public void run() {
                    if (archive.delete()) {
                        finish();
                    }
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // ...
                }
            }
        );
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit:
            	Intent intent = new Intent(this, Modify.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                startActivity(intent);
                break;

            case R.id.menu_delete:
                confirmDelete();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    
    
    /**
     * Take screenshot from tabhost for sharing
     * 
     * @return
     */
    private Bitmap getRouteBitmap() {
    	View view = findViewById(R.id.detail_all);
    	view.setDrawingCacheEnabled(true);
    	view.buildDrawingCache();
    	view.destroyDrawingCache();
    	view.setDrawingCacheQuality(100);
    	return Bitmap.createBitmap(view.getDrawingCache());
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();

        Intent mapIntent = new Intent(this, BaiduMap.class);
        String name = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        mapIntent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, name);
        mapIntent.putExtra(INSIDE_TABHOST, true);
        
        mTabHost.setup(localActivityManager);
        mTabHost.clearAllTabs();

        TabHost.TabSpec tabSpec =
            mTabHost.newTabSpec("").setIndicator("").setContent(mapIntent);
        mTabHost.addTab(tabSpec);

        localActivityManager.dispatchResume();
        if (!archive.exists()) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mTabHost.clearAllTabs();
        localActivityManager.removeAllActivities();
        localActivityManager.dispatchPause(isFinishing());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    

    private void addArchiveMetaTimeFragment() {
        addFragment(R.id.archive_meta_time_distance_layout, archiveMetaTimeDistanceFragment);
    }

    private void addArchiveMetaFragment() {
        addFragment(R.id.archive_meta_detail_layout, archiveMetaDetailFragment);
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
            Intent intent = new Intent(this, BaiduMapDetail.class);
            intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, Modify.class);
		intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
		startActivity(intent);
		
	}
}
