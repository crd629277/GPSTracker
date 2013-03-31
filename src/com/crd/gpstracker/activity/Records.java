package com.crd.gpstracker.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.Activity;
import com.crd.gpstracker.dao.Archive;
import com.crd.gpstracker.dao.ArchiveMeta;
import com.crd.gpstracker.service.ArchiveNameHelper;

public class Records extends Activity implements AdapterView.OnItemClickListener {
    private Context context;
    public static final String INTENT_ARCHIVE_FILE_NAME = "name";

    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archive> archives;

    private ArchiveNameHelper archiveFileNameHelper;
    private ArchivesAdapter archivesAdapter;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Archive archive = archives.get(i);
        Intent intent = new Intent(this, Detail.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_ARCHIVE_FILE_NAME, archive.getName());

        startActivity(intent);
    }

    /**
     * 
     * ListView 的 Adapter
     *
     */
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
            
            mDistance.setText(String.format(getString(R.string.records_formatter), archiveMeta.getDistance() / archiveMeta.TO_KILOMETRE));
            
            mCostTime.setText(archiveMeta.getRawCostTimeString());
            
            String description = archiveMeta.getDescription();
            if (description.length() <= 0) {
                description = getString(R.string.no_description);
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

        getArchiveFilesByMonth(new Date());
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

    private void getArchiveFilesByMonth(Date date) {
        archiveFileNames = archiveFileNameHelper.getArchiveFilesNameByMonth(date);
        openArchivesFromFileNames();
    }
    
    /**
     *  从指定目录读取所有已保存的列表
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

    /**
     * 清除列表
     */
    private void closeArchives() {
        Iterator<Archive> iterator = archives.iterator();
        while (iterator.hasNext()) {
            Archive archive = (Archive) iterator.next();
            if(archive != null) {
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

