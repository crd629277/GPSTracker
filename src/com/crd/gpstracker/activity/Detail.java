package com.crd.gpstracker.activity;

import java.io.File;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapView;
import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.MapActivity;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;

public class Detail extends MapActivity implements View.OnClickListener, MKSearchListener {
    private String archiveFileName;
    private ActionBar actionBar;
    private Archive archive;
    private ArchiveMeta archiveMeta;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mDistance;
    private TextView mSpeed;
    private TextView mRecords;
    private EditText mDescription;
    private Button mButton;
    private SimpleDateFormat formatter;
    private TextView mArchiveName;
    private TextView mMaxSpeed;
    private Context context;
    private UIHelper uiHelper;
    
    private TextView mapMask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        context = this;
        mapView = (MapView) findViewById(R.id.bmapsView);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        
        actionBar =  (ActionBar) findViewById(R.id.action_bar);
        mArchiveName = (TextView) findViewById(R.id.archive_name);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);
        mDistance = (TextView) findViewById(R.id.distance);
        mRecords = (TextView) findViewById(R.id.records);
        mSpeed = (TextView) findViewById(R.id.speed);
        mMaxSpeed = (TextView) findViewById(R.id.max_speed);
        mDescription = (EditText) findViewById(R.id.description);
        mButton = (Button) findViewById(R.id.update);
        mapMask = (TextView) findViewById(R.id.map_mask);
        
        
        formatter = new SimpleDateFormat(getString(R.string.time_format));
        uiHelper = new UIHelper(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        setCenterPoint(archive.getLastRecord(), false);
        mapViewController.setZoom(14);
        
        mapMask.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					Intent intent = new Intent(context, BaiduMap.class);
					intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archive.getName());
					startActivity(intent);
				}
				return true;
			}
		});
        
        String description = archiveMeta.getDescription().trim();
        if(description.length() > 0) {
        	mDescription.setText(description);
        } else {
        	MKSearch search = new MKSearch();
        	search.init(bMapManager, this);
        	search.reverseGeocode(getRealGeoPointFromLocation(archive.getLastRecord()));
        }
        
        

        mArchiveName.setText(archive.getName());
        mStartTime.setText(formatter.format(archiveMeta.getStartTime()));
        mEndTime.setText(formatter.format(archiveMeta.getEndTime()));

        mDistance.setText(String.valueOf(archiveMeta.getDistance()));
        mRecords.setText(String.valueOf(archiveMeta.getCount()));
        mSpeed.setText(String.valueOf(archiveMeta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        mMaxSpeed.setText(String.valueOf(archiveMeta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        

        mButton.setOnClickListener(this);

        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return android.R.drawable.ic_menu_delete;
            }

            @Override
            public void performAction(View view) {
            	uiHelper.showConfirmDialog(getString(R.string.delete),
                        String.format(getString(R.string.sure_to_del), archiveFileName),
                        new Runnable() {
                            @Override
                            public void run() {
                                if (archive.delete()) {
                                    uiHelper.showShortToast(String.format(getString(R.string.has_deleted), archiveFileName));
                                } else {
                                    uiHelper.showLongToast(getString(R.string.delete_error));
                                }
                                finish();
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {

                            }
                        }
                    );
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!archive.exists()) {
        	finish();
        }
    }


    @Override
    public void onDestroy() {
        if (archive != null) {
            archive.close();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        String description = mDescription.getText().toString().trim();
        if (description.length() > 0 && archiveMeta.setDescription(description)) {
            uiHelper.showShortToast(getString(R.string.updated));
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onGetAddrResult(MKAddrInfo mkAddrInfo, int arg1) {
		String address = mkAddrInfo.strAddr;
		uiHelper.showLongToast(address);
		archiveMeta.setDescription(String.format(getString(R.string.nearby), address));
		
	}

	@Override
	public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
