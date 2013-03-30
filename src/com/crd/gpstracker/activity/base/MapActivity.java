package com.crd.gpstracker.activity.base;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.CoordinateConvert;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.crd.gpstracker.R;
import com.crd.gpstracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;
import com.umeng.analytics.MobclickAgent;

public abstract class MapActivity extends com.baidu.mapapi.MapActivity implements MKGeneralListener {
    protected MapView mapView = null;
    protected MapController mapViewController;
    static protected BMapManager bMapManager;
    private static final String BAIDU_MAP_KEY = "353EEEC233F62E4062BA1E3A87A9468141B21AEE";
    private static boolean running = false;
    protected UIHelper uiHelper;
    protected Context context;
    private ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        
        bMapManager = new BMapManager(getApplication());
        bMapManager.init(BAIDU_MAP_KEY, this);

        actionBar = (ActionBar) findViewById(R.id.action_bar);
        uiHelper = new UIHelper(context);
        MobclickAgent.onError(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mapView == null) {
            return;
        }

        mapView.removeAllViews();
        super.initMapActivity(bMapManager);

        mapViewController = mapView.getController();
        if(!running) {
        	bMapManager.start();
        	running = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(running) {
        	bMapManager.stop();
        	running = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    protected void setCenterPoint(Location location, boolean animate) {
        if (mapView == null) {
            return;
        }

        GeoPoint geoPoint = getRealGeoPointFromLocation(location);

        // @todo 自动计算默认缩放的地图界面
        if (animate) {
            mapViewController.animateTo(geoPoint);
        } else {
            mapViewController.setCenter(geoPoint);
        }
    }

    protected void setCenterPoint(Location location) {
        setCenterPoint(location, false);
    }
    
    protected GeoPoint getGeoPoint(Location location) {
    	GeoPoint geoPoint = new GeoPoint(
                (int) (location.getLatitude() * 1E6),
                (int) (location.getLongitude() * 1E6)
            );
    	return geoPoint;
	}
    
    protected GeoPoint getRealGeoPointFromLocation(Location location) {
        GeoPoint geoPoint = getGeoPoint(location);
        return CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
    }

    @Override
    public void onDestroy() {
        bMapManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetNetworkState(int iError) {

    }

    @Override
    public void onGetPermissionState(int iError) {
    	if(iError == MKEvent.ERROR_PERMISSION_DENIED) {
    		
    	}
    }
}
