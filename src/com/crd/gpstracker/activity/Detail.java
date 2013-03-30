package com.crd.gpstracker.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.markupartist.android.widget.ActionBar;

public class Detail extends Base implements View.OnClickListener {
    private String archiveFileName;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        archiveMeta = archive.getMeta();

        setContentView(R.layout.detail);

        mArchiveName = (TextView) findViewById(R.id.archive_name);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);
        mDistance = (TextView) findViewById(R.id.distance);
        mRecords = (TextView) findViewById(R.id.records);
        mSpeed = (TextView) findViewById(R.id.speed);
        mMaxSpeed = (TextView) findViewById(R.id.max_speed);
        mDescription = (EditText) findViewById(R.id.description);
        mButton = (Button) findViewById(R.id.update);

        formatter = new SimpleDateFormat(getString(R.string.time_format));
    }

    @Override
    public void onStart() {
        super.onStart();

        mArchiveName.setText(archive.getName());
        mStartTime.setText(formatter.format(archiveMeta.getStartTime()));
        mEndTime.setText(formatter.format(archiveMeta.getEndTime()));

        mDistance.setText(String.valueOf(archiveMeta.getDistance()));
        mRecords.setText(String.valueOf(archiveMeta.getCount()));
        mSpeed.setText(String.valueOf(archiveMeta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        mMaxSpeed.setText(String.valueOf(archiveMeta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
        mDescription.setText(archiveMeta.getDescription());

        mButton.setOnClickListener(this);

        actionBar.removeAllActions();
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return android.R.drawable.ic_input_get;
            }

            @Override
            public void performAction(View view) {
                Intent intent = new Intent(context, BaiduMap.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archive.getName());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        File file = new File(archiveFileName);
        if (!file.exists()) {
            finish();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (archive != null) {
            archive.close();
        }
    }

    @Override
    public void onClick(View view) {
        String description = mDescription.getText().toString().trim();
        if (description.length() > 0 && archiveMeta.setDescription(description)) {
            uiHelper.showShortToast(getString(R.string.updated));
        }
    }
}
