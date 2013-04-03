package com.crd.gpstracker.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.Activity;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.service.ArchiveNameHelper;
import com.markupartist.android.widget.ActionBar.Action;

public class Records extends Activity implements
		AdapterView.OnItemClickListener, DatePickerDialog.OnDateSetListener {
	private Context context;
	public static final String INTENT_ARCHIVE_FILE_NAME = "name";
	public static final String INTENT_SELECT_BY_MONTH = "month";

	private ListView listView;
	private ArrayList<String> archiveFileNames;
	private ArrayList<Archive> archives;

	private ArchiveNameHelper archiveFileNameHelper;
	private ArchivesAdapter archivesAdapter;
	private long selectedTime;

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Archive archive = archives.get(i);
		Intent intent = new Intent(this, Detail.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(INTENT_ARCHIVE_FILE_NAME, archive.getName());

		startActivity(intent);
	}
	

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.set(year, month, day);

		Date selectDate = new Date(selectedTime);
		if (selectDate.getMonth() != month) {
			Intent intent = new Intent(context, Records.class);
			intent.putExtra(INTENT_SELECT_BY_MONTH, calendar.getTimeInMillis());
			startActivity(intent);
		}
	}

	
	public class ArchivesAdapter extends ArrayAdapter<Archive> {

		public ArchivesAdapter(ArrayList<Archive> archives) {
			super(context, R.layout.records_row, archives);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Archive archive = archives.get(position);
			ArchiveMeta archiveMeta = archive.getMeta();

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.records_row, parent, false);

			TextView mDescription = (TextView) rowView.findViewById(R.id.description);
			TextView mCostTime = (TextView) rowView.findViewById(R.id.cost_time);
			TextView mDistance = (TextView) rowView.findViewById(R.id.distance);

			mDistance.setText(String.format(
					getString(R.string.records_formatter), archiveMeta.getDistance() / archiveMeta.TO_KILOMETRE));

			String costTime = archiveMeta.getRawCostTimeString();
			mCostTime.setText(costTime.length() > 0 ? costTime : getString(R.string.not_available));

			String description = archiveMeta.getDescription();
			if (description.length() <= 0) {
				description = getString(R.string.no_description);
				mDescription.setTextColor(getResources().getColor(R.color.gray));
			}
			mDescription.setText(description);

			return rowView;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.records);

		this.context = getApplicationContext();
		this.listView = (ListView) findViewById(R.id.records_list);
		this.archiveFileNameHelper = new ArchiveNameHelper(context);

		archives = new ArrayList<Archive>();
		archivesAdapter = new ArchivesAdapter(archives);
		listView.setAdapter(archivesAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		listView.setOnItemClickListener(this);
		
		actionBar.removeAllActions();
		actionBar.addAction(new Action() {
			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_today;
			}
			
			@Override
			public void performAction(View view) {
				showTimeSelectDialog();
			}

		});

		selectedTime = getIntent().getLongExtra(INTENT_SELECT_BY_MONTH, System.currentTimeMillis());
		
		// setAction title as month string if there is not current month
		actionBar.setTitle(R.string.title_records);
		SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.time_month_format));
		String selectedTitle = formatter.format(new Date(selectedTime));
		if(!selectedTitle.equals(formatter.format(new Date()))) {
			actionBar.setTitle(selectedTitle);
		}
		getArchiveFilesByMonth(new Date(selectedTime));
	}

	@Override
	public void onStop() {
		super.onStop();
		closeArchives();
	}

	@Override
	public void onResume() {
		super.onResume();
		archivesAdapter.notifyDataSetChanged();

	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }
	

	private void showTimeSelectDialog() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(new Date(selectedTime));

        DatePickerDialog datePicker = new DatePickerDialog(
            Records.this, Records.this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

	private void getArchiveFilesByMonth(Date date) {
		archiveFileNames = archiveFileNameHelper.getArchiveFilesNameByMonth(date);
		openArchivesFromFileNames();
	}

	/**
	 * 从指定目录读取所有已保存的列表
	 * 
	 * @throws IOException
	 */
	private void openArchivesFromFileNames() {
		Iterator<String> iterator = archiveFileNames.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();

			Archive archive = new Archive(context, name);

			if (archive.getMeta().getCount() > 0) {
				archives.add(archive);
			}
		}
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar:
                showTimeSelectDialog();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    

	/**
	 * 清除列表
	 */
	private void closeArchives() {
		Iterator<Archive> iterator = archives.iterator();
		while (iterator.hasNext()) {
			Archive archive = (Archive) iterator.next();
			if (archive != null) {
				archive.close();
			}
		}

		archives.clear();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
