package com.crd.runtracker.activity.maps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.crd.runtracker.R;
import com.crd.runtracker.activity.Detail;
import com.crd.runtracker.activity.Records;
import com.crd.runtracker.activity.base.MapActivity;
import com.crd.runtracker.dao.Archive;

public class BaiduMap extends MapActivity {
	private Archive archive;

	private Context context;

	private String archiveFileName;
	private PathOverlay pathOverlay;
	private PointMarkLayout currentMarkLayout;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baidu_map);

		context = this;

		mapView = (MapView) findViewById(R.id.bmapsView);
		archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);

		archive = new Archive(getApplicationContext(), archiveFileName);
		locations = archive.fetchAll();

		// 计算边界
		getBoundary();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		
		Intent intent = getIntent();
		if(intent.getBooleanExtra(Detail.INSIDE_TABHOST, false)) {
			if(actionBar != null) {
				actionBar.setVisibility(View.GONE);
			}
		}

		mapView.getOverlays().add(new PathOverlay());
		mapView.getOverlays().add(new PointMarkLayout(archive.getFirstRecord(), R.drawable.point_start));
		mapView.getOverlays().add(new PointMarkLayout(archive.getLastRecord(), R.drawable.point_end));

		// @todo 自动计算默认缩放的地图界面
		mapViewController.setCenter(mapCenterPoint);
		mapViewController.setZoom(getFixedZoomLevel());
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

	private class PathOverlay extends Overlay {
		private Paint paint;
		private Projection projection;
		private static final int MIN_POINT_SPAN = 3;

		public PathOverlay() {
			setPaint();
		}

		private void setPaint() {
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setDither(true);

			paint.setColor(getResources().getColor(R.color.red));
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(5);
			paint.setAlpha(188);
		}

		@Override
		public void draw(final Canvas canvas, final MapView mapView,
				boolean shadow) {
			this.projection = mapView.getProjection();

			if (!shadow) {
				synchronized (canvas) {
					final Path path = new Path();
					final int maxWidth = mapView.getWidth();
					final int maxHeight = mapView.getHeight();

					Point lastGeoPoint = null;
					for (Location location : locations) {
						Point current = projection.toPixels(getRealGeoPointFromLocation(location), null);

						if (lastGeoPoint != null
								&& (lastGeoPoint.y < maxHeight && lastGeoPoint.x < maxWidth)) {
							// if(Math.abs(current.x - lastGeoPoint.x) <
							// MIN_POINT_SPAN
							// || Math.abs(current.y - lastGeoPoint.y) <
							// MIN_POINT_SPAN ) {
							// continue;
							// } else {
							path.lineTo(current.x, current.y);
							// }

						} else {
							path.moveTo(current.x, current.y);
						}
						lastGeoPoint = current;
					}

					canvas.drawPath(path, paint);
				}
			}

		}
	}

	private class PointMarkLayout extends Overlay {
		private Location location;
		private int drawable;
		private Projection projection;

		public PointMarkLayout(Location location, int drawable) {
			this.location = location;
			this.drawable = drawable;
		}

		@Override
		public void draw(final Canvas canvas, final MapView mapView,
				boolean shadow) {
			super.draw(canvas, mapView, shadow);

			this.projection = mapView.getProjection();
			Point point = projection.toPixels(getRealGeoPointFromLocation(location), null);

			Bitmap markerImage = BitmapFactory.decodeResource(getResources(), drawable);

			// 根据实际的条目而定偏移位置
			canvas.drawBitmap(markerImage,
					point.x - Math.round(markerImage.getWidth() * 0.4), 
					point.y - Math.round(markerImage.getHeight() * 0.9), null);

			return;
		}

	}

}
