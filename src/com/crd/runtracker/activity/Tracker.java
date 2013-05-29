package com.crd.runtracker.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.crd.runtracker.R;
import com.crd.runtracker.activity.base.Activity;
import com.crd.runtracker.dao.ArchiveMeta;
import com.crd.runtracker.fragment.ArchiveMetaFragment;
import com.crd.runtracker.service.Recorder;
import com.crd.runtracker.util.Helper.Logger;
import com.markupartist.android.widget.ActionBar;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

public class Tracker extends Activity implements View.OnClickListener,
		View.OnLongClickListener {
	private Button mStartButton;
	private Button mEndButton;

	private ArchiveMetaFragment archiveMetaFragment;

	protected ArchiveMeta archiveMeta;

	private static final int FLAG_RECORDING = 0x001;
	private static final int FLAG_ENDED = 0x002;
	// private static final int FLAG_PAUSE = 0x003;
	private static final long MINI_RECORDS = 2;

	private boolean isRecording = false;
	public static final int MESSAGE_UPDATE_VIEW = 0x011;
	private Timer updateViewTimer;
	private static final long TIMER_PERIOD = 1000;
	private TextView mCoseTime;
	private Button mDisabledButton;
	private Spinner mSpinner;
	private ArrayAdapter<CharSequence> activityTypeAdapter;
	private TextView mActivityTypeView;
	private int mActivityTypePosition;

	class SpinnerSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mActivityTypePosition = position;
			// helper.showLongToast("position: " + position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracker);

		mStartButton = (Button) findViewById(R.id.btn_start);
		mEndButton = (Button) findViewById(R.id.btn_end);
		mDisabledButton = (Button) findViewById(R.id.btn_disabled);

		mStartButton.setOnClickListener(this);
		mEndButton.setOnClickListener(this);
		mDisabledButton.setOnClickListener(this);
		mEndButton.setOnLongClickListener(this);

		mCoseTime = (TextView) findViewById(R.id.item_cost_time);
		mActivityTypeView = (TextView) findViewById(R.id.activity_type_text);

		mSpinner = (Spinner) findViewById(R.id.activity_type_spinner);
		// activityTypeAdapter = ArrayAdapter.createFromResource(this,
		// R.array.activityType, android.R.layout.simple_spinner_item);
		activityTypeAdapter = ArrayAdapter.createFromResource(this,
				R.array.activityType, R.layout.activity_type_spinner);
		// activityTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		activityTypeAdapter
				.setDropDownViewResource(R.layout.activity_type_dropdown_item);
		mSpinner.setAdapter(activityTypeAdapter);
		mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		mSpinner.setVisibility(View.VISIBLE);

		// Check update from umeng
		UmengUpdateAgent.update(context);
		UMFeedbackService.enableNewReplyNotification(context,
				NotificationType.AlertDialog);
	}

	private void notifyUpdateView() {
		Message message = new Message();
		message.what = MESSAGE_UPDATE_VIEW;
		uiHandler.sendMessage(message);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (helper.isGPSProvided()) {
			updateViewTimer = new Timer();
			updateViewTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					notifyUpdateView();
				}
			}, 0, TIMER_PERIOD);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!helper.isGPSProvided()) {
			mStartButton.setVisibility(View.GONE);
			mEndButton.setVisibility(View.GONE);
			mDisabledButton.setVisibility(View.VISIBLE);

			helper.showLongToast(getString(R.string.gps_not_presented));
		} else {
			mDisabledButton.setVisibility(View.GONE);
		}

		// 设置 ActionBar 样式
		actionBar.setTitle(getString(R.string.app_name));
		actionBar.removeAllActions();
		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.clearHomeAction();
		actionBar.setHomeAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_home;
            }

            @Override
            public void performAction(View view) {
//                finish();
            }
        });
		actionBar.addAction(new ActionBar.Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_recent_history;
			}

			@Override
			public void performAction(View view) {
				gotoActivity(Records.class);

			}

		});

		actionBar.addAction(new ActionBar.Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_manage;
			}

			@Override
			public void performAction(View view) {
				gotoActivity(Preference.class);

			}

		});
	}

	private void gotoActivity(java.lang.Class cls) {
		Intent intent = new Intent(context, cls);
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_start:
			if (serviceBinder != null && !isRecording) {
				serviceBinder.startRecord(mActivityTypePosition);
				notifyUpdateView();
			}
			break;
		case R.id.btn_end:
			helper.showShortToast(getString(R.string.long_press_to_stop));
			break;

		case R.id.btn_disabled:
			Intent intent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			break;
		}
	}

	@Override
	public boolean onLongClick(View view) {
		if (isRecording && serviceBinder != null) {

			serviceBinder.stopRecord();
			notifyUpdateView();

			if (archiveMeta != null) {
				long count = archiveMeta.getCount();
				if (count >= MINI_RECORDS) {
					Intent intent = new Intent(context, Modify.class);
					intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME,
							archiveMeta.getName());
					startActivity(intent);
				}
			}

			serviceBinder.closeDB();
		}
		setViewStatus(FLAG_ENDED);
		return true;
	}

	private void setViewStatus(int status) {
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		switch (status) {
		case FLAG_RECORDING:
			mCoseTime.setVisibility(View.VISIBLE);
			mActivityTypeView.setVisibility(View.GONE);
			mSpinner.setVisibility(View.GONE);
			mStartButton.setVisibility(View.GONE);
			mEndButton.setVisibility(View.VISIBLE);
			if (archiveMeta != null) {
				archiveMetaFragment = new ArchiveMetaFragment(context,
						archiveMeta);
				fragmentTransaction.replace(R.id.status_layout,
						archiveMetaFragment);

				mCoseTime.setText(archiveMeta.getCostTimeStringByNow());
				// mCoseTime.setText(serviceBinder.getCostTime());
			}
			break;
		case FLAG_ENDED:
			mCoseTime.setVisibility(View.GONE);
			mActivityTypeView.setVisibility(View.VISIBLE);
			mSpinner.setVisibility(View.VISIBLE);
			mStartButton.setVisibility(View.VISIBLE);
			mEndButton.setVisibility(View.GONE);
			if (archiveMetaFragment != null) {
				fragmentTransaction.remove(archiveMetaFragment);
			}
			mCoseTime.setText(R.string.none_cost_time);
			break;
		}

		fragmentTransaction.commit();
	}

	// 控制界面显示 UI
	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATE_VIEW:
				if (serviceBinder == null) {
					Logger.i(getString(R.string.not_available));
					return;
				}

				archiveMeta = serviceBinder.getMeta();

				switch (serviceBinder.getStatus()) {
				case Recorder.ServiceBinder.STATUS_RECORDING:
					setViewStatus(FLAG_RECORDING);
					isRecording = true;
					break;
				case Recorder.ServiceBinder.STATUS_STOPPED:
					setViewStatus(FLAG_ENDED);
					isRecording = false;
				}
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tracker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_records:
			gotoActivity(Records.class);
			break;

		case R.id.menu_feedback:
			UMFeedbackService.openUmengFeedbackSDK(context);
			break;

		case R.id.menu_about:
			gotoActivity(Info.class);
			break;

		case R.id.menu_exit:
			exit();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	public void exit() {
		if (isRecording) {
			helper.showConfirmDialog(getString(R.string.menu_exit),
					getString(R.string.can_not_exit), new Runnable() {
						@Override
						public void run() {
							
						}
					}, new Runnable() {
						@Override
						public void run() {
							// ...
						}
					});
		} else {
			serviceBinder.closeDB();
			finish();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (updateViewTimer != null) {
			updateViewTimer.cancel();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isRecording) {
			helper.showLongToast(getString(R.string.still_running));
		}
	}

}
