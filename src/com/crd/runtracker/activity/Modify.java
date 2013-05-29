package com.crd.runtracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.crd.runtracker.R;
import com.crd.runtracker.activity.base.Activity;
import com.crd.runtracker.dao.Archive;
import com.crd.runtracker.dao.ArchiveMeta;
import com.markupartist.android.widget.ActionBar;

public class Modify extends Activity implements View.OnClickListener{

	private Button mBtnConfirm;
	private EditText mDescription;
	
	private String archiveFileName;
	private Archive archive;
	private ArchiveMeta archiveMeta;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify);
		
		mBtnConfirm = (Button) findViewById(R.id.confirm);
		mDescription = (EditText) findViewById(R.id.description);
		
		archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
		
	}

	@Override
    public void onStart() {
        super.onStart();

        archive = new Archive(context, archiveFileName, Archive.MODE_READ_WRITE);
        if (archive == null || !archive.exists()) {
            helper.showShortToast(getString(R.string.archive_not_exists));
            finish();
            return;
        }
        archiveMeta = archive.getMeta();
        
        actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(getString(R.string.title_modify));
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

        mDescription.setText(archiveMeta.getDescription());
        mBtnConfirm.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        if (archive != null) {
            archive.close();
        }
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        String description = mDescription.getText().toString().trim();
        try {
            if (description.length() > 0 && archiveMeta.setDescription(description)) {
//                helper.showLongToast(getString(R.string.has_benn_saved));
                Intent intent = new Intent(context, Detail.class);
				intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
				startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            helper.showLongToast(getString(R.string.shit_happens));
        }
    }
}
