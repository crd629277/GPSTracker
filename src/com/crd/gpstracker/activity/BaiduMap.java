package com.crd.gpstracker.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.CoordinateConvert;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.OverlayItem;
import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.MapActivity;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;


public class BaiduMap extends MapActivity implements SeekBar.OnSeekBarChangeListener{
    private Archive archive;
    private Context context;
    private ArrayList<Location> locations;
    private BMapManager bMapManager = null;
    private String archiveFileName;
    private SeekBar mSeekBar;
    private SimpleDateFormat dateFormat;
    private ToggleButton mSatellite;

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		try {
			Location location = locations.get(seekBar.getProgress() - 1);
			uiHelper.showShortToast(dateFormat.format(location.getTime()));
			setCenterPoint(location, true);
		} catch (Exception e) {
			return;
		}
		
	}
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baidu_map);

        context = this;

        mapView = (MapView) findViewById(R.id.bmapsView);

        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
        mSeekBar = (SeekBar) findViewById(R.id.seek);
        mSatellite = (ToggleButton) findViewById(R.id.satellite);

        uiHelper = new UIHelper(context);
        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        
        dateFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.CHINA);
        
        archive = new Archive(getApplicationContext(), archiveFileName);
        locations = archive.fetchAll();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        int size = locations.size();
        if (size <= 0) {
            return;
        }
        
        mSeekBar.setMax(locations.size());
        mSeekBar.setProgress(0);
        mSeekBar.setOnSeekBarChangeListener(this);
        
        mSatellite.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSatellite.isChecked()) {
					mapView.setSatellite(true);
				} else {
					mapView.setSatellite(false);
				}
				
				bMapManager.stop();
				bMapManager.start();
				uiHelper.showShortToast(getString(R.string.toggle_satellite));
			}
		});
        
        Location firstLocation = locations.get(0);

        Drawable marker = getResources().getDrawable(R.drawable.mark);
        mapView.getOverlays().add(new RouteItemizedOverlay(marker, context));
        

        //float distance = firstLocation.distanceTo(lastLocation);

        // @todo 自动计算默认缩放的地图界面
        setCenterPoint(firstLocation, false);
        mapViewController.setZoom(16);
    }

    @Override
    public void onPause() {
        super.onResume();
    }
    
    @Override
    public void onStop() {
    	mapView.getOverlays().clear();
    	super.onStop();
    }

    @Override
    public void onDestroy() {
        archive.close();
        super.onDestroy();
    }
    
    
    class RouteItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    	Bitmap bitmap;
    	
    	private List<OverlayItem> geoPointList = new ArrayList<OverlayItem>();
    	private Paint paint;

		public RouteItemizedOverlay(Drawable marker, Context context) {
			super(boundCenterBottom(marker));
			
			for(int i=0; i < locations.size(); i++) {
				Location x = locations.get(i);
				GeoPoint geoPoint = getRealGeoPointFromLocation(x);
				geoPointList.add(new OverlayItem(geoPoint, x.getLatitude() + "", x.getLongitude() + ""));
			}
			
//			paint = new Paint();
//			paint.setAntiAlias(true);
//			paint.setColor(Color.RED);
//			paint.setAlpha(95);
//			paint.setStrokeWidth(6);
			
			populate();
		}
		
//		@Override
//        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//            Projection projection = mapView.getProjection();
//
//            GeoPoint lastGeoPoint = null;
//            bitmap = Bitmap.createBitmap(mapView.getWidth(), mapView.getHeight(), Bitmap.Config.ARGB_8888);
//
//            Canvas tmpCanvas = new Canvas(bitmap);
//            for (int i = 0; i < locations.size(); i++) {
//                Location x = locations.get(i);
//
//                GeoPoint geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
//                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
//
//                Point current = projection.toPixels(geoPoint, null);
//                if(lastGeoPoint != null) {
//                	Point last = projection.toPixels(lastGeoPoint, null);
//                	
//                    canvas.drawLine(last.x, last.y, current.x, current.y, paint);
//                } else {
//					tmpCanvas.drawPoint(current.x, current.y, paint);
//				}
//                
//                lastGeoPoint = geoPoint;
//            }
//
//            canvas.drawBitmap(bitmap, 0, 0, null);
//        }

		@Override
		protected OverlayItem createItem(int i) {
			return geoPointList.get(i);
		}

		@Override
		public int size() {
			return geoPointList.size();
		}
		
		@Override
		protected boolean onTap(int i) {
			Location location = locations.get(i);
			uiHelper.showShortToast(dateFormat.format(location.getTime()));
			mSeekBar.setProgress(i);
			setCenterPoint(location, true);
			return true;
		}
    	
    }

}
